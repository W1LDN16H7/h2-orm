#!/bin/bash

# H2-ORM Build Script
# Comprehensive build and release automation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="H2-ORM"
VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
BUILD_DIR="target"
DIST_DIR="dist"
DOCS_DIR="docs"

# Functions
print_header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    print_success "Java: $(java -version 2>&1 | head -n 1)"

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    print_success "Maven: $(mvn -version | head -n 1)"

    # Check Git
    if ! command -v git &> /dev/null; then
        print_warning "Git is not installed - version control features will be limited"
    else
        print_success "Git: $(git --version)"
    fi
}

# Clean build
clean_build() {
    print_header "Cleaning Previous Build"
    mvn clean
    rm -rf "$DIST_DIR"
    mkdir -p "$DIST_DIR"
    print_success "Build cleaned"
}

# Run tests
run_tests() {
    print_header "Running Tests"
    print_info "Running unit tests..."
    mvn test

    print_info "Running integration tests..."
    mvn verify

    print_success "All tests passed"
}

# Build project
build_project() {
    print_header "Building Project"
    mvn compile
    print_success "Project compiled successfully"
}

# Package project
package_project() {
    print_header "Packaging Project"
    mvn package -DskipTests
    print_success "Project packaged successfully"
}

# Generate documentation
generate_docs() {
    print_header "Generating Documentation"

    # Generate Javadocs
    print_info "Generating Javadocs..."
    mvn javadoc:javadoc

    # Copy docs to dist
    if [ -d "target/site/apidocs" ]; then
        cp -r target/site/apidocs "$DIST_DIR/javadocs"
        print_success "Javadocs generated and copied to $DIST_DIR/javadocs"
    fi

    # Generate code coverage report
    print_info "Generating code coverage report..."
    mvn jacoco:report

    if [ -d "target/site/jacoco" ]; then
        cp -r target/site/jacoco "$DIST_DIR/coverage"
        print_success "Coverage report generated and copied to $DIST_DIR/coverage"
    fi
}

# Create distribution
create_distribution() {
    print_header "Creating Distribution"

    # Copy main JAR
    cp target/h2-orm-*.jar "$DIST_DIR/" 2>/dev/null || true

    # Copy sources JAR
    cp target/h2-orm-*-sources.jar "$DIST_DIR/" 2>/dev/null || true

    # Copy Javadoc JAR
    cp target/h2-orm-*-javadoc.jar "$DIST_DIR/" 2>/dev/null || true

    # Copy complete JAR (with dependencies)
    cp target/h2-orm-*-complete.jar "$DIST_DIR/" 2>/dev/null || true

    # Create release notes
    cat > "$DIST_DIR/RELEASE_NOTES.md" << EOF
# $PROJECT_NAME v$VERSION Release Notes

## 📦 Distribution Contents

- \`h2-orm-$VERSION.jar\` - Main library JAR
- \`h2-orm-$VERSION-sources.jar\` - Sources JAR
- \`h2-orm-$VERSION-javadoc.jar\` - Javadoc JAR
- \`h2-orm-$VERSION-complete.jar\` - Complete JAR with all dependencies
- \`javadocs/\` - API documentation
- \`coverage/\` - Code coverage reports

## 🚀 Features

- Spring Boot-style repository pattern
- Support for H2, MySQL, PostgreSQL, SQLite
- Advanced backup and export functionality
- Sorting and pagination support
- Field-based query methods
- Table management utilities
- Comprehensive test coverage

## 📖 Usage

Add to your Maven dependencies:

\`\`\`xml
<dependency>
    <groupId>com.h2.orm</groupId>
    <artifactId>h2-orm</artifactId>
    <version>$VERSION</version>
</dependency>
\`\`\`

## 🔧 Quick Start

\`\`\`java
// Start H2-ORM
H2ORM.startInMemory();

// Get repository
JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);

// Use like Spring Data JPA
User user = new User("john", "john@example.com", "John Doe");
userRepo.save(user);

List<User> users = userRepo.findByField("username", "john");
Page<User> page = userRepo.findAll(PageRequest.of(0, 10, Sort.by("username")));
\`\`\`

Built on $(date)
EOF

    print_success "Distribution created in $DIST_DIR/"
}

# Run quality checks
run_quality_checks() {
    print_header "Running Quality Checks"

    print_info "Running SpotBugs analysis..."
    mvn spotbugs:check || print_warning "SpotBugs found some issues (non-blocking)"

    print_info "Checking dependencies for vulnerabilities..."
    mvn org.owasp:dependency-check-maven:check || print_warning "Dependency check completed with warnings"

    print_success "Quality checks completed"
}

# Create checksums
create_checksums() {
    print_header "Creating Checksums"

    cd "$DIST_DIR"
    for file in *.jar; do
        if [ -f "$file" ]; then
            sha256sum "$file" > "$file.sha256"
            md5sum "$file" > "$file.md5"
        fi
    done
    cd ..

    print_success "Checksums created"
}

# Print build summary
print_summary() {
    print_header "Build Summary"
    echo -e "${GREEN}🎉 Build completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}Project:${NC} $PROJECT_NAME v$VERSION"
    echo -e "${BLUE}Build Date:${NC} $(date)"
    echo -e "${BLUE}Distribution:${NC} $DIST_DIR/"
    echo ""
    echo -e "${BLUE}Generated Files:${NC}"
    ls -la "$DIST_DIR/" | grep -E '\.(jar|md|sha256|md5)$' | while read line; do
        echo "  $line"
    done
    echo ""
    print_info "Run './release.sh' to create a release package"
}

# Main build function
main() {
    print_header "🚀 Building $PROJECT_NAME v$VERSION"

    # Parse command line arguments
    SKIP_TESTS=false
    SKIP_DOCS=false
    QUICK_BUILD=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --skip-docs)
                SKIP_DOCS=true
                shift
                ;;
            --quick)
                QUICK_BUILD=true
                SKIP_TESTS=true
                SKIP_DOCS=true
                shift
                ;;
            --help)
                echo "Usage: $0 [OPTIONS]"
                echo "Options:"
                echo "  --skip-tests    Skip running tests"
                echo "  --skip-docs     Skip generating documentation"
                echo "  --quick         Quick build (skip tests and docs)"
                echo "  --help          Show this help message"
                exit 0
                ;;
            *)
                echo "Unknown option $1"
                exit 1
                ;;
        esac
    done

    # Execute build steps
    check_prerequisites
    clean_build
    build_project

    if [ "$SKIP_TESTS" = false ]; then
        run_tests
    else
        print_warning "Skipping tests"
    fi

    package_project

    if [ "$SKIP_DOCS" = false ]; then
        generate_docs
    else
        print_warning "Skipping documentation generation"
    fi

    if [ "$QUICK_BUILD" = false ]; then
        run_quality_checks
    fi

    create_distribution
    create_checksums
    print_summary
}

# Run main function
main "$@"
