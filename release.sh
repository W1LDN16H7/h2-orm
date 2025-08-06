#!/bin/bash

# H2-ORM Release Script
# Creates release packages and handles version management

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
PROJECT_NAME="H2-ORM"
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
DIST_DIR="dist"
RELEASE_DIR="releases"

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

# Check if build exists
check_build() {
    if [ ! -d "$DIST_DIR" ]; then
        print_error "Build directory not found. Run ./build.sh first."
        exit 1
    fi

    if [ ! -f "$DIST_DIR/h2-orm-$CURRENT_VERSION.jar" ]; then
        print_error "Build artifacts not found. Run ./build.sh first."
        exit 1
    fi

    print_success "Build artifacts found"
}

# Create release package
create_release_package() {
    print_header "Creating Release Package"

    mkdir -p "$RELEASE_DIR"
    RELEASE_PACKAGE="$RELEASE_DIR/h2-orm-$CURRENT_VERSION-release.tar.gz"

    # Create temporary directory for release
    TEMP_DIR=$(mktemp -d)
    RELEASE_NAME="h2-orm-$CURRENT_VERSION"
    mkdir -p "$TEMP_DIR/$RELEASE_NAME"

    # Copy distribution files
    cp -r "$DIST_DIR"/* "$TEMP_DIR/$RELEASE_NAME/"

    # Copy documentation
    cp README.md "$TEMP_DIR/$RELEASE_NAME/" 2>/dev/null || true
    cp LICENSE "$TEMP_DIR/$RELEASE_NAME/" 2>/dev/null || true
    cp CHANGELOG.md "$TEMP_DIR/$RELEASE_NAME/" 2>/dev/null || true

    # Create installation script
    cat > "$TEMP_DIR/$RELEASE_NAME/install.sh" << 'EOF'
#!/bin/bash

# H2-ORM Installation Script

echo "🚀 Installing H2-ORM..."

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "📦 Installing to local Maven repository..."
    mvn install:install-file \
        -Dfile=h2-orm-*.jar \
        -DgroupId=com.h2.orm \
        -DartifactId=h2-orm \
        -Dversion=* \
        -Dpackaging=jar
    echo "✅ Installed to Maven local repository"
else
    echo "⚠️  Maven not found. Manual installation required."
    echo "   Add the JAR file to your project's classpath manually."
fi

echo "📖 See RELEASE_NOTES.md for usage instructions"
EOF
    chmod +x "$TEMP_DIR/$RELEASE_NAME/install.sh"

    # Create Windows installation script
    cat > "$TEMP_DIR/$RELEASE_NAME/install.bat" << 'EOF'
@echo off
echo 🚀 Installing H2-ORM...

mvn --version >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Maven not found. Manual installation required.
    echo    Add the JAR file to your project's classpath manually.
    pause
    exit /b 1
)

echo 📦 Installing to local Maven repository...
for %%f in (h2-orm-*.jar) do (
    mvn install:install-file -Dfile=%%f -DgroupId=com.h2.orm -DartifactId=h2-orm -Dversion=%VERSION% -Dpackaging=jar
)

echo ✅ Installed to Maven local repository
echo 📖 See RELEASE_NOTES.md for usage instructions
pause
EOF

    # Create archive
    cd "$TEMP_DIR"
    tar -czf "$RELEASE_PACKAGE" "$RELEASE_NAME"
    cd - > /dev/null

    # Cleanup
    rm -rf "$TEMP_DIR"

    print_success "Release package created: $RELEASE_PACKAGE"
}

# Create GitHub release format
create_github_release() {
    print_header "Creating GitHub Release Assets"

    GITHUB_DIR="$RELEASE_DIR/github-v$CURRENT_VERSION"
    mkdir -p "$GITHUB_DIR"

    # Copy main artifacts
    cp "$DIST_DIR/h2-orm-$CURRENT_VERSION.jar" "$GITHUB_DIR/"
    cp "$DIST_DIR/h2-orm-$CURRENT_VERSION-sources.jar" "$GITHUB_DIR/"
    cp "$DIST_DIR/h2-orm-$CURRENT_VERSION-javadoc.jar" "$GITHUB_DIR/"
    cp "$DIST_DIR/h2-orm-$CURRENT_VERSION-complete.jar" "$GITHUB_DIR/"

    # Copy checksums
    cp "$DIST_DIR"/*.sha256 "$GITHUB_DIR/" 2>/dev/null || true
    cp "$DIST_DIR"/*.md5 "$GITHUB_DIR/" 2>/dev/null || true

    # Create release notes for GitHub
    cat > "$GITHUB_DIR/release-notes.md" << EOF
# H2-ORM v$CURRENT_VERSION

🎉 **New Release of H2-ORM - Modern Spring Boot-style ORM Library**

## 📦 What's Included

- **Main Library**: \`h2-orm-$CURRENT_VERSION.jar\` - Core library
- **Complete Bundle**: \`h2-orm-$CURRENT_VERSION-complete.jar\` - Library with all dependencies
- **Sources**: \`h2-orm-$CURRENT_VERSION-sources.jar\` - Source code
- **Documentation**: \`h2-orm-$CURRENT_VERSION-javadoc.jar\` - API documentation

## 🚀 Key Features

- 🔄 **Spring Boot-style Repository Pattern** - Familiar API for Spring developers
- 🗄️ **Multi-Database Support** - H2, MySQL, PostgreSQL, SQLite
- 📊 **Advanced Query Methods** - Field-based queries, sorting, pagination
- 💾 **Backup & Export** - CSV, Excel, JSON export with database backup
- 🛠️ **Table Management** - Create, drop, truncate tables programmatically
- 🔒 **Transaction Management** - Robust transaction handling
- 📝 **Comprehensive Testing** - Extensive test coverage

## 🛠️ Maven Dependency

\`\`\`xml
<dependency>
    <groupId>com.h2.orm</groupId>
    <artifactId>h2-orm</artifactId>
    <version>$CURRENT_VERSION</version>
</dependency>
\`\`\`

## 📖 Quick Start

\`\`\`java
// 1. Start H2-ORM
H2ORM.startInMemory();

// 2. Get repository (just like Spring Data JPA)
JpaRepository<User, Long> userRepo = H2ORM.repository(User.class);

// 3. Use familiar repository methods
User user = new User("john_doe", "john@example.com", "John Doe");
userRepo.save(user);

// 4. Query with field-based methods
List<User> users = userRepo.findByField("username", "john_doe");
Page<User> page = userRepo.findAll(PageRequest.of(0, 10, Sort.by("username")));

// 5. Export data
H2ORM.exportToCsv(User.class, "./users.csv");
H2ORM.backup("./backup");
\`\`\`

## 🔧 Advanced Features

### Field-Based Queries
\`\`\`java
// Find by single field
List<Order> processed = orderRepo.findByField("status", OrderStatus.PROCESSED);

// Find by multiple fields
Map<String, Object> criteria = Map.of(
    "status", OrderStatus.PROCESSED,
    "customer", specificUser
);
List<Order> orders = orderRepo.findByFields(criteria);

// Range queries
List<Order> expensive = orderRepo.findByFieldBetween("amount", 100.0, 1000.0);

// Text queries
List<User> users = userRepo.findByFieldContaining("email", "@gmail.com");
\`\`\`

### Pagination & Sorting
\`\`\`java
// Basic pagination
Page<User> page = userRepo.findAll(PageRequest.of(0, 20));

// With sorting
Page<User> sorted = userRepo.findAll(
    PageRequest.of(0, 20, Sort.by("username"))
);

// Complex sorting
Sort complexSort = Sort.by("status")
    .and(Sort.by(Sort.Direction.DESC, "createdDate"));
\`\`\`

### Export & Backup
\`\`\`java
// Export to various formats
H2ORM.exportToCsv(User.class, "./exports/users.csv");
H2ORM.exportToExcel(Product.class, "./exports/products.xlsx");
H2ORM.exportToJson(Order.class, "./exports/orders.json");

// Database backup
H2ORM.backup("./backups/daily_backup");
H2ORM.backupCompressed("./backups/compressed_backup");
\`\`\`

## 🗄️ Database Support

| Database | Status | JDBC URL Example |
|----------|--------|------------------|
| H2 | ✅ Full Support | \`jdbc:h2:mem:testdb\` |
| MySQL | ✅ Full Support | \`jdbc:mysql://localhost:3306/db\` |
| PostgreSQL | ✅ Full Support | \`jdbc:postgresql://localhost:5432/db\` |
| SQLite | ✅ Full Support | \`jdbc:sqlite:database.db\` |

## 📋 Requirements

- Java 21 or higher
- Maven 3.6+ (for building from source)

## 🔗 Links

- [GitHub Repository](https://github.com/W1LDN16H7/h2-orm)
- [Documentation](https://github.com/W1LDN16H7/h2-orm/wiki)
- [Issues](https://github.com/W1LDN16H7/h2-orm/issues)

## 🛡️ Verification

All artifacts are signed and checksummed:
- SHA256 checksums: \`*.sha256\` files
- MD5 checksums: \`*.md5\` files

---

**Built on:** $(date)
**License:** MIT
**Maintainer:** W1LDN16H7
EOF

    print_success "GitHub release assets created in $GITHUB_DIR/"
}

# Generate Maven deployment commands
generate_maven_commands() {
    print_header "Generating Maven Deployment Commands"

    cat > "$RELEASE_DIR/maven-deploy-commands.txt" << EOF
# Maven Deployment Commands for H2-ORM v$CURRENT_VERSION

## Deploy to Maven Central (requires proper setup)
mvn clean deploy -P release

## Deploy to local repository
mvn install:install-file \\
    -Dfile=h2-orm-$CURRENT_VERSION.jar \\
    -DgroupId=com.h2.orm \\
    -DartifactId=h2-orm \\
    -Dversion=$CURRENT_VERSION \\
    -Dpackaging=jar

## Deploy sources
mvn install:install-file \\
    -Dfile=h2-orm-$CURRENT_VERSION-sources.jar \\
    -DgroupId=com.h2.orm \\
    -DartifactId=h2-orm \\
    -Dversion=$CURRENT_VERSION \\
    -Dpackaging=jar \\
    -Dclassifier=sources

## Deploy javadoc
mvn install:install-file \\
    -Dfile=h2-orm-$CURRENT_VERSION-javadoc.jar \\
    -DgroupId=com.h2.orm \\
    -DartifactId=h2-orm \\
    -Dversion=$CURRENT_VERSION \\
    -Dpackaging=jar \\
    -Dclassifier=javadoc

## Create GPG signatures (for Maven Central)
gpg --armor --detach-sig h2-orm-$CURRENT_VERSION.jar
gpg --armor --detach-sig h2-orm-$CURRENT_VERSION-sources.jar
gpg --armor --detach-sig h2-orm-$CURRENT_VERSION-javadoc.jar
EOF

    print_success "Maven deployment commands saved to $RELEASE_DIR/maven-deploy-commands.txt"
}

# Version management
bump_version() {
    local version_type=$1

    print_header "Bumping Version ($version_type)"

    case $version_type in
        major)
            mvn versions:set -DnextMajorVersion=true
            ;;
        minor)
            mvn versions:set -DnextMinorVersion=true
            ;;
        patch)
            mvn versions:set -DnextIncrementalVersion=true
            ;;
        *)
            print_error "Invalid version type. Use: major, minor, or patch"
            exit 1
            ;;
    esac

    # Clean up backup files
    find . -name "*.versionsBackup" -delete

    local new_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
    print_success "Version bumped from $CURRENT_VERSION to $new_version"

    # Commit version change if git is available
    if command -v git &> /dev/null; then
        git add pom.xml
        git commit -m "Bump version to $new_version"
        print_success "Version change committed to git"
    fi
}

# Main function
main() {
    print_header "🚀 H2-ORM Release Manager v$CURRENT_VERSION"

    case "${1:-release}" in
        release)
            check_build
            create_release_package
            create_github_release
            generate_maven_commands

            print_header "🎉 Release Complete!"
            echo -e "${GREEN}Release packages created:${NC}"
            echo "  📦 $RELEASE_DIR/h2-orm-$CURRENT_VERSION-release.tar.gz"
            echo "  🐙 $RELEASE_DIR/github-v$CURRENT_VERSION/"
            echo "  📝 $RELEASE_DIR/maven-deploy-commands.txt"
            ;;
        bump-major)
            bump_version major
            ;;
        bump-minor)
            bump_version minor
            ;;
        bump-patch)
            bump_version patch
            ;;
        --help)
            echo "Usage: $0 [COMMAND]"
            echo "Commands:"
            echo "  release       Create release packages (default)"
            echo "  bump-major    Bump major version"
            echo "  bump-minor    Bump minor version"
            echo "  bump-patch    Bump patch version"
            echo "  --help        Show this help message"
            ;;
        *)
            print_error "Unknown command: $1"
            echo "Run '$0 --help' for usage information"
            exit 1
            ;;
    esac
}

main "$@"
