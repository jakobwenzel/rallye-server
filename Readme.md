This is the Server for the Stadtrallye organized by the [Fachschaft IST](http://www.fs-ist.de)

To run the server you will need to create a config file named `config.json` in one of the following locations:

* Current working dir
* Directory containing the jar
* Top Project dir (the directory containing this file, only if a .git directory is found)
* User home dir, here the file must be named `.rallyeserv-config.json`

Example config file:
All values that are printed are defaults, values in `<>` must be changed for the server to work.

	{
		"hostName" : "<HOST>",
		"restPort" : 10101,
		"consolePort" : 10100,

		"dbConnectionConfig" : {
			"connectString" : "jdbc:mysql://<HOST>/<DATABASE>?characterEncoding=utf8",
			"maxIdleTime" : 3600,
			"password" : "<USER>",
			"username" : "<PASSWORD>"
		},

		"serverName" : "<SERVER_NAME>",
		"description" : "<SERVER_DESCRIPTION>",

		"dataRelativeToConfig": true,
		"dataDirectory": "data/",

		"gcmApiKey" : "<GOOGLE_CLOUD_MESSAGING_KEY>",

		"mapConfig" : {
			"name" : "<MAP_NAME>",
			"location" : {
				"latitude" : <>,
				"longitude" : <>
			},
			"bounds" : [
				{
					"latitude" : <>,
					"longitude" : <>
				},
				{
					"latitude" : <>,
					"longitude" : <>
				}
			],
			"zoomLevel" : 13
		},
		"imageCacheConfig" : {
			"maxThumbEntries" : 100,
			"maxMiniEntries" : 25
		}
	}

If `dataRelativeToConfig` is set, the used `dataDirectory` will be a concatenation of the location of the config file and the given value.