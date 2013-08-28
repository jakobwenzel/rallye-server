This is the Server for the Stadtrallye organized by the [Fachschaft IST](http://www.fs-ist.de)

To run the server you will need to create a config file named `config.json` in one of the following locations:
x  Current working dir
x  Directory containing the jar
x  User home dir, here the file must be named `.rallyeserv-config.json`

Example config file:
All values that are printed are defaults, values in `<>` must be changed for the server to work.

	{ "consolePort" : 10100,
	  "dbConnection" : { "connectString" : "jdbc:mysql://<HOST>/<DATABASE>?characterEncoding=utf8",
	      "maxIdleTime" : 3600,
	      "password" : "<PASSWORD>",
	      "username" : "<USERNAME>"
	    },
	  "description" : "<DESCRIPTION>",
	  "gcmKey" : "<KEY>",
	  "hostName" : "0.0.0.0",
	  "mapBounds" : [ { "latitude" : 49.858958999999999,
	        "longitude" : 8.6351069999999996
	      },
	      { "latitude" : 49.892369100000003,
	        "longitude" : 8.6746797999999998
	      }
	    ],
	  "mapCenter" : { "latitude" : 49.877648000000001,
	      "longitude" : 8.6517619999999997
	    },
	  "restPort" : 10101,
	  "serverName" : "<NAME>"
	}

