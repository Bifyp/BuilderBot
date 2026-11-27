<div align="center">

~~~text
██████╗ ██████╗  ██████╗      ██╗███████╗ ██████╗████████╗
██╔══██╗██╔══██╗██╔═══██╗     ██║██╔════╝██╔════╝╚══██╔══╝
██████╔╝██████╔╝██║   ██║     ██║█████╗  ██║        ██║
██╔══██╗██╔══██╗██║   ██║██   ██║██╔══╝  ██║        ██║
██████╔╝██║  ██║╚██████╔╝╚█████╔╝███████╗╚██████╗   ██║
╚═════╝ ╚═╝  ╚═╝ ╚═════╝  ╚════╝ ╚══════╝ ╚═════╝   ╚═╝
~~~

**// BUILDERBOT · PROJECT README**

*source · setup · architecture · security*

[![README style](https://img.shields.io/badge/README-Codex%20Style-00D9FF?style=for-the-badge)](https://github.com/Bifyp/codex-agent-workbench)

</div>

---

## > PROJECT.OVERVIEW

This is the English project guide for **BuilderBot**. The repository-specific implementation notes remain available in [README.md](README.md).

## > QUICK.BOOT

1. Clone the repository.
2. Read the project-specific setup section in [README.md](README.md).
3. Install only the dependencies required by this project.
4. Keep credentials in local environment variables or a secrets manager.
5. Run the project tests before deployment.

## > SECURITY.PROTOCOL

Never commit `.env` files, API keys, bot tokens, Telegram sessions, private certificates, database dumps, generated logs, or local model profiles. Review third-party provider terms and deployment permissions before publishing.

## > DOCUMENTATION.MAP

- [Russian / original README](README.md)
- [Repository source](.)
- [Codex Agent Workbench style reference](https://github.com/Bifyp/codex-agent-workbench)

## > STATUS

Documentation format standardized across Bifyp repositories.

## > BOT.PERMISSIONS

- `/bot spawn <name>` and `/bot remove <name>` require Minecraft permission level **2** or higher, even for a bot's owner.
- `/bot <name> status|stop|pause|resume|wait` require the creating player's UUID or permission level **2** or higher. Name matching is case-insensitive; ownership uses UUIDs, not player names.
- `/bot list` remains public and shows bot names, not their task/status details.
- Console and other non-player command sources need permission level **2**; they never receive player ownership privileges.
- A bot stays owned by its creator if that player loses operator status; they can still use its named commands but cannot spawn/remove bots.

Security regression tests use Java 21, JUnit, Mockito, and Fabric's JUnit launcher. Run `gradle test` (or `./gradlew test` when the wrapper is available).
