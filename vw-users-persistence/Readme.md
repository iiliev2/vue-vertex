This module uses the proxy service verticle to define and deploy a DAO for UserDTOs. The default implementation uses simple in memory storage(a map).

The UsersPersistenceInMemVerticle will try to locate a file called users.json to load data from. When undeployed, it will backup the old file, and overwrite the new contents on it.