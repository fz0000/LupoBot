[![CodeFactor](https://www.codefactor.io/repository/github/lupobot/lupobot/badge)](https://www.codefactor.io/repository/github/lupobot/lupobot)
[![tokei](https://tokei.rs/b1/github/LupoBot/LupoBot)](https://github.com/XAMPPRocky/tokei)
[![License](https://img.shields.io/github/license/LupoBot/LupoBot)](https://github.com/LupoBot/LupoBot/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/lupobot/localized.svg)](https://crowdin.com/project/lupobot)

# LupoBot

###

<p align="center">
    <img src="https://i.imgur.com/QuW4Jqf.png" height="100">
    <h3 align="center">LupoBot</h3>
    <p align="center">
       An all-in-all Discord Bot that offers many cool new features in plugins which you can install to your Discord server!
       <br/>
       <a href="https://discord.gg/SPezacNufS" target="_blank">Discord</a> • <a href="https://crowdin.com/project/lupobot" target="_blank">Crowdin</a> • <a href="https://github.com/LupoBot/LupoBot/issues" target="_blank">Bugs & Suggestions</a> 
</p>

###

## Summary
This repository tracks bugreports and suggestions in the issue tab. You are free to contribute with a Pull Request. **If you have any questions etc., please join our [Discord server](https://discord.gg/SPezacNufS).**

1. [Installation](#installation)
2. [API](#api)
3. [REST API](#rest-api)

## Installation
### Setup
Clone the repository.
```shell
git clone https://github.com/LupoBot/LupoBot.git
```
Build the project, requires [Java](https://www.java.com/de/) 14 and [Apache Maven](https://maven.apache.org):
```shell
mvn -B package --file pom.xml
```
Put the core and the launcher into the same directory. Then you can run the launcher and the directory structure should look like this:
```bash
├── configs
│   └── ...
├── logs
│   └── ...
├── plugins
│   └── ...
├── core.jar (file name can be different but must contain core)
└── launchar.jar (file name can be different)
```

### Configuration
Til LupoBot is not configured correctly, only the launcher and not the core will start. [MongoDB](https://www.mongodb.com) as database is required. Please note, that you have to create a Discord application in the [Discord Developer Portal](https://discord.com/developers/applications).
After the configs are filled out correctly, the launcher can be started again and LupoBot should be running.

## API
Our core offers an API to create own plugins. All plugins can be found in the [lupobot-plugins](https://github.com/LupoBot/LupoBot/tree/master/lupobot-plugins) project in which each plugin has its own project. The ``pom.xml`` of the plugin project needs the core as dependency and should use the Maven Shade Plugin. [Click here for an example pom.xml.](https://github.com/LupoBot/LupoBot/blob/master/lupobot-plugins/lupobot-plugin-fun/pom.xml)

### Main Class
The main class extends ``LupoPlugin`` and in it, you can run your code when the plugin is being enabled in the ``onEnable()`` and disabled in the ``onDisable()`` method. There are also a few util methods to register listeners with ``registerListeners("...")`` and commands with ``registerCommands("...")``.

The ``@PluginInfo`` annotation needs to be applied to the class.

**Example:**
```java
@PluginInfo(name = "logging", author = "Nickkel")
public class LupoLoggingPlugin extends LupoPlugin {

    @Getter
    public static LupoLoggingPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.logging.commands");
        this.registerListener("de.nickkel.lupobot.plugin.logging.listener");
    }

    @Override
    public void onDisable() {

    }
}
```

### Plugin Data
Each plugin can store data. There are different types of data which are stored in the database:
- Bot (``LupoBot.getInstance().getData()``)
- User (``LupoUser.getByMember(member)``)
- Server (``LupoServer.getByGuild(guild)``)

You can get the BascDBObject by using the ``getData()`` method of the LupoUser or LupoServer class. You can only get data which exists, otherwise it will be null. To store data for your plugin, you need to create a JSON file in the resources folder called ``bot.json`` (bot data), ``user.json`` (user data) or ``server.json``. In this JSON file, you can set data keys and their default values.

**Example:**
```json
{
  "lastTicketId": 0,
  "supportTeamRoles": [],
  "limitAmount": 1,
  "visibleEveryone": false,
  "creationMessage": -1,
  "notifyChannel": -1,
  "claimedCategory": -1,
  "openedCategory": -1,
  "closedCategory": -1,
  "tickets": {}
}
```

Each plugin data will be merged into one final data ``getData()`` file, which could look like this (the plugin data is in an object called like the plugin):
```json
{
  "logging": {
    "lastTicketId": 0,
    "supportTeamRoles": [],
    "limitAmount": 1,
    "visibleEveryone": false,
    "creationMessage": -1,
    "notifyChannel": -1,
    "claimedCategory": -1,
    "openedCategory": -1,
    "closedCategory": -1,
    "tickets": {}
  },
  "tickets": {
    "...": "..."
  }
}
```
To get the plugin data directly, you can use the ``getPluginData(plugin, "key")`` method in each of these classes instead of ``getData()``.

### Localization
It is important to localize your plugin. The strings and the language files should be found in the ``resources/locales`` folder of the plugin project. As source language, we use English so that there should be a ``en_US.properties`` file. You can use ``%prefix%`` as variable for the server prefix.

The locale names start with the plugin name, it has the following schema:
```properties
<plugin-name>_your-locale-name = Your string
```
Please note, that each command needs at least three locales with general information.

**Example:**
```properties
fun_eightball-description = Get an answer to your question
fun_eightball-usage = %prefix%eightball <question>
fun_eightball-example = %prefix%eightball Do you like LupoBot?
```
To get a locale, simply get the server with ``LupoServer.getByGuild(guild)`` and use the ``translate(plugin, "locale", params...)`` method.

## REST API
LupoBot offers a RESTful API as interface for other services, like our website. The REST API (version: 1) has `GET` and `POST` http requests. By default, it is available on port 7000 (configurable) on the `/v1` path:

HTTP request | Route | Explanation
------------ |-------| -----------
GET | /bot | Get information about the bot
GET | /bot/status | Get the status of the bot
POST | /bot/status/:key | Set the status of the bot (`ONLINE`, `IDLE`, `DO_NOT_DISTURB`, `INVISIBLE`, `OFFLINE`)
GET | /bot/activity-name | Get the current activity name of the bot
POST | /bot/activity-name/:name | Set the activity name of the bot
POST | /bot/update-commands | Update all Slash Commands (register them again if the options etc. have been changed)
GET | /commands | Get all loaded commands
POST | /commands/:name | Enable or disable a command. The query param `type` is required (the type can be "disable" or "enable")
GET | /commands/:name | Get a specific command by its name
POST | /guilds/:id/messages | Create or edit a message on a guild. The query params `id` (= guild id) and `channelId` are required, the param `messageId` is optional if you want to edit a message and don't want to create a new one. Optional params to build the message or embed: `content, description, thumbnail, authorName, authorIconUrl, authorUrl, title, titleUrl, footer, footerIconUrl, image, color` (as int)
GET | /guilds/:id/channels | Get all channels on a guild by guild id
GET | /guilds/:id/channels/:channel | Get a specific channel on a guild by guild id and channel id
GET | /guilds/:id/roles | Get all roles on a guild by guild id
GET | /guilds/:id/roles/:role | Get a specific role on a guild by guild id and role id
GET | /guilds/members/:member | Get a specific role on a guild by member id
GET | /oauth2 | Get information about the user and the guilds which the has joined. The query params `code` (of Discord OAuth2) and `redirect` (redirect url after auth) are required
GET | /plugins | Get all loaded plugins
POST | /plugins/:name | Unload or reload a plugin by name. The query param `type` is required (the type can be "reload" or "unload")
GET | /plugins/:name | Get a specific plugin by its name
GET | /servers | Get all cached servers
GET | /servers/total | Get the total server count (also uncached servers)
POST | /servers/:id | Edit server data by guild id. The params `key` and `value` are required, `plugin` (= plugin name) is optional. If the param plugin is not given, the data will be set as core data (e.g. the prefix). Only the value of keys which exist in the server data can be edited!
GET | /servers/:id | Get a specific server by guild id (if it is not cached, it will be loaded)
GET | /shards | Get all shards
GET | /shards/total | Get the total shard count
GET | /shards/:id | Get a shard by id (starts with 0)
POST | /shards/:id | Shutdown or restart a shard by id. The query param `type` is required (the type can be "shutdown" or "restart")
GET | /users | Get all cached users
GET | /users/total | Get the total user count (also uncached users)
POST | /users/:id | Edit user data by member/user id. The params `key` and `value` are required, `plugin` (= plugin name) is optional. If the param plugin is not given, the data will be set as core data. Only the value of keys which exist in the user data can be edited!
GET | /users/:id | Get a specific user by guild id (if it is not cached, it will be loaded)