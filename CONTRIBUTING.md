# Contributing to SimpleClan

First off, thank you for considering contributing to SimpleClan! It's people like you that make SimpleClan such a great tool.

## Code of Conduct

By participating in this project, you are expected to uphold our Code of Conduct:
- Be respectful and inclusive
- Be patient and welcoming
- Focus on what is best for the community

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples to demonstrate the steps**
* **Describe the behavior you observed after following the steps**
* **Explain which behavior you expected to see instead and why**
* **Include screenshots if possible**
* **Include your environment details:**
  - Minecraft version
  - Server software (Spigot/Paper) and version
  - Java version
  - SimpleClan version
  - Other relevant plugins

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a step-by-step description of the suggested enhancement**
* **Provide specific examples to demonstrate the steps**
* **Describe the current behavior and explain which behavior you expected to see instead**
* **Explain why this enhancement would be useful**

### Pull Requests

* Fill in the required template
* Do not include issue numbers in the PR title
* Follow the Java code style used throughout the project
* Include screenshots and animated GIFs in your pull request whenever possible
* Document new code based on the existing documentation style
* End all files with a newline

## Development Setup

### Prerequisites

- Java 21 or newer
- Maven 3.6+
- IntelliJ IDEA or Eclipse (recommended)
- Git

### Getting Started

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/tremeq/SimpleClan.git
   cd SimpleClan
   ```

3. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. Make your changes

5. Test your changes:
   ```bash
   mvn clean package
   ```

6. Commit your changes:
   ```bash
   git commit -m "Add some feature"
   ```

7. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

8. Create a Pull Request

## Code Style Guidelines

### Java Code Style

* Use 4 spaces for indentation (no tabs)
* Use meaningful variable and method names
* Add comments for complex logic
* Follow Java naming conventions:
  - Classes: `PascalCase`
  - Methods: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Variables: `camelCase`

### Example:

```java
public class ClanManager {
    private static final int MAX_MEMBERS = 10;
    private final SimpleClan plugin;

    public ClanManager(SimpleClan plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new clan
     * @param player The player creating the clan
     * @param name The clan name
     * @return true if successful, false otherwise
     */
    public boolean createClan(Player player, String name) {
        // Implementation
    }
}
```

### Commit Message Guidelines

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

### Example Commit Messages:

```
Add clan chat toggle feature

- Implement /clan cc command
- Add chat manager to handle toggle state
- Update language files with new messages

Fixes #123
```

## Language File Contributions

When adding new features that require user-facing text:

1. Add the English translation to `src/main/resources/lang/en.yml`
2. Add the Polish translation to `src/main/resources/lang/pl.yml`
3. Use descriptive keys in the format: `category.subcategory.key`

Example:
```yaml
# en.yml
chat:
  toggled-on: "&aAuto clan chat enabled!"
  toggled-off: "&cAuto clan chat disabled!"

# pl.yml
chat:
  toggled-on: "&aAuto czat klanowy włączony!"
  toggled-off: "&cAuto czat klanowy wyłączony!"
```

## Testing

Before submitting a pull request:

1. Ensure the project compiles without errors:
   ```bash
   mvn clean package
   ```

2. Test your changes on a local Minecraft server:
   - Test with Minecraft 1.21+
   - Test with and without PlaceholderAPI
   - Test all affected commands
   - Test both Polish and English languages

3. Verify no regressions in existing features

## Documentation

* Update the README.md if you add/change features
* Update CHANGELOG.md following the format
* Add JavaDoc comments for public methods
* Update language files for new messages

## Questions?

Feel free to open an issue with the label `question` if you need help or clarification.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to SimpleClan! 🎉
