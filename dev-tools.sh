#!/bin/bash

# H2-ORM Development Helper Utilities
# Provides common development tasks and utilities

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }

# Quick development setup
dev_setup() {
    print_header "Setting Up Development Environment"

    print_info "Installing Maven dependencies..."
    mvn dependency:resolve

    print_info "Downloading sources..."
    mvn dependency:sources

    print_info "Downloading javadocs..."
    mvn dependency:resolve -Dclassifier=javadoc

    print_success "Development environment ready"
}

# Run tests with coverage
test_with_coverage() {
    print_header "Running Tests with Coverage"

    mvn clean test jacoco:report

    if [ -d "target/site/jacoco" ]; then
        print_success "Coverage report available at: target/site/jacoco/index.html"

        # Extract coverage percentage
        if command -v grep &> /dev/null && [ -f "target/site/jacoco/index.html" ]; then
            COVERAGE=$(grep -o '[0-9]*%' target/site/jacoco/index.html | head -1)
            print_info "Code coverage: $COVERAGE"
        fi
    fi
}

# Format code
format_code() {
    print_header "Formatting Code"

    # Check if google-java-format is available
    if command -v google-java-format &> /dev/null; then
        find src -name "*.java" -exec google-java-format --replace {} \;
        print_success "Code formatted with google-java-format"
    else
        print_warning "google-java-format not found. Install it for automatic formatting."
        print_info "Install: https://github.com/google/google-java-format"
    fi
}

# Check dependencies for updates
check_dependencies() {
    print_header "Checking Dependencies for Updates"

    mvn versions:display-dependency-updates
    mvn versions:display-plugin-updates

    print_info "Review the output above for available updates"
}

# Security scan
security_scan() {
    print_header "Running Security Scan"

    print_info "Checking for vulnerable dependencies..."
    mvn org.owasp:dependency-check-maven:check

    if [ -f "target/dependency-check-report.html" ]; then
        print_success "Security report available at: target/dependency-check-report.html"
    fi
}

# Generate project statistics
project_stats() {
    print_header "Project Statistics"

    echo -e "${BLUE}📊 Code Statistics:${NC}"

    # Lines of code
    if command -v find &> /dev/null && command -v wc &> /dev/null; then
        JAVA_FILES=$(find src -name "*.java" | wc -l)
        JAVA_LINES=$(find src -name "*.java" -exec wc -l {} \; | tail -1 | awk '{print $1}')
        TEST_FILES=$(find src/test -name "*.java" 2>/dev/null | wc -l)
        TEST_LINES=$(find src/test -name "*.java" -exec wc -l {} \; 2>/dev/null | tail -1 | awk '{print $1}' || echo "0")

        echo "  Java Files: $JAVA_FILES"
        echo "  Java Lines: $JAVA_LINES"
        echo "  Test Files: $TEST_FILES"
        echo "  Test Lines: $TEST_LINES"
    fi

    # Maven info
    echo -e "${BLUE}📦 Maven Info:${NC}"
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    GROUP_ID=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)
    ARTIFACT_ID=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)

    echo "  Group ID: $GROUP_ID"
    echo "  Artifact ID: $ARTIFACT_ID"
    echo "  Version: $VERSION"

    # Dependencies
    echo -e "${BLUE}🔗 Dependencies:${NC}"
    mvn dependency:list | grep -E "^\[INFO\] +[a-zA-Z]" | wc -l | xargs echo "  Total Dependencies:"
}

# Clean all generated files
deep_clean() {
    print_header "Deep Clean"

    mvn clean
    rm -rf target/
    rm -rf dist/
    rm -rf releases/
    rm -rf logs/
    rm -rf .mvn/

    # Clean IDE files
    rm -rf .idea/
    rm -rf *.iml
    rm -rf .vscode/
    rm -rf .eclipse/
    rm -rf bin/

    print_success "Deep clean completed"
}

# Database cleanup
clean_databases() {
    print_header "Cleaning Test Databases"

    rm -rf data/*.db
    rm -rf data/*.mv.db
    rm -rf data/*.trace.db
    rm -rf *.db
    rm -rf *.mv.db
    rm -rf *.trace.db

    print_success "Test databases cleaned"
}

# Performance benchmark
benchmark() {
    print_header "Running Performance Benchmark"

    print_info "Compiling benchmark classes..."
    mvn test-compile

    print_info "Running benchmarks..."
    # This would run JMH benchmarks if available
    mvn exec:java -Dexec.mainClass="h2.orm.benchmark.PerformanceBenchmark" 2>/dev/null || {
        print_warning "No benchmark classes found"
        print_info "Create benchmark classes in src/test/java/h2/orm/benchmark/"
    }
}

# Generate changelog
generate_changelog() {
    print_header "Generating Changelog"

    if command -v git &> /dev/null; then
        # Get git tags and commits
        CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

        cat > CHANGELOG.md << EOF
# Changelog

All notable changes to this project will be documented in this file.

## [${CURRENT_VERSION}] - $(date +%Y-%m-%d)

### Added
- Initial release of H2-ORM
- Spring Boot-style repository pattern
- Multi-database support (H2, MySQL, PostgreSQL, SQLite)
- Advanced backup and export functionality
- Sorting and pagination support
- Field-based query methods
- Table management utilities
- Comprehensive test coverage

### Features
- 🔄 Repository pattern similar to Spring Data JPA
- 🗄️ Support for multiple databases
- 📊 Advanced querying with sorting and pagination
- 💾 Export to CSV, Excel, JSON formats
- 🛠️ Database backup and restore
- 🔍 Field-based query methods
- 📋 Table management operations

### Technical Details
- Java 21+ compatibility
- Hibernate 6.x integration
- HikariCP connection pooling
- Jackson JSON processing
- Apache POI Excel support
- SLF4J logging
- JUnit 5 testing

EOF

        print_success "Changelog generated"
    else
        print_error "Git not available - cannot generate changelog"
    fi
}

# IDE setup helper
setup_ide() {
    print_header "Setting Up IDE Configuration"

    # IntelliJ IDEA
    if [ ! -d ".idea" ]; then
        mkdir -p .idea

        cat > .idea/compiler.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="CompilerConfiguration">
    <bytecodeTargetLevel target="21" />
  </component>
</project>
EOF

        cat > .idea/misc.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
  <component name="ProjectRootManager" version="2" languageLevel="JDK_21" default="true" project-jdk-name="21" project-jdk-type="JavaSDK">
    <output url="file://$PROJECT_DIR$/out" />
  </component>
</project>
EOF

        print_success "IntelliJ IDEA configuration created"
    fi

    # VS Code
    if [ ! -d ".vscode" ]; then
        mkdir -p .vscode

        cat > .vscode/settings.json << 'EOF'
{
    "java.configuration.updateBuildConfiguration": "interactive",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
    "files.exclude": {
        "**/target": true,
        "**/.classpath": true,
        "**/.project": true,
        "**/.settings": true,
        "**/.factorypath": true
    }
}
EOF

        cat > .vscode/launch.json << 'EOF'
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Run H2ORM Tests",
            "request": "launch",
            "mainClass": "h2.orm.test.H2ORMTest",
            "projectName": "h2-orm"
        }
    ]
}
EOF

        print_success "VS Code configuration created"
    fi
}

# Main function
main() {
    case "${1:-help}" in
        setup)
            dev_setup
            ;;
        test)
            test_with_coverage
            ;;
        format)
            format_code
            ;;
        deps)
            check_dependencies
            ;;
        security)
            security_scan
            ;;
        stats)
            project_stats
            ;;
        clean)
            deep_clean
            ;;
        clean-db)
            clean_databases
            ;;
        benchmark)
            benchmark
            ;;
        changelog)
            generate_changelog
            ;;
        ide)
            setup_ide
            ;;
        all)
            print_header "Running All Development Tasks"
            dev_setup
            test_with_coverage
            format_code
            security_scan
            project_stats
            generate_changelog
            print_success "All development tasks completed"
            ;;
        help|--help)
            echo "H2-ORM Development Helper Utilities"
            echo ""
            echo "Usage: $0 [COMMAND]"
            echo ""
            echo "Commands:"
            echo "  setup       Set up development environment"
            echo "  test        Run tests with coverage"
            echo "  format      Format code"
            echo "  deps        Check dependency updates"
            echo "  security    Run security scan"
            echo "  stats       Show project statistics"
            echo "  clean       Deep clean all generated files"
            echo "  clean-db    Clean test databases"
            echo "  benchmark   Run performance benchmarks"
            echo "  changelog   Generate changelog"
            echo "  ide         Set up IDE configuration"
            echo "  all         Run all development tasks"
            echo "  help        Show this help message"
            ;;
        *)
            print_error "Unknown command: $1"
            echo "Run '$0 help' for usage information"
            exit 1
            ;;
    esac
}

main "$@"
