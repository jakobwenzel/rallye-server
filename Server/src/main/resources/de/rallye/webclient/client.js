function findRoot() {

	var loc = document.location.href;
	var pos = loc.indexOf('/',8);
	return loc.substr(0,pos+1);

}

function setup_error_handler() {

	$(document).bind("ajaxError",function(event, xhr, ajaxOptions, thrownError) {
		var readable;
		console.log(xhr.status);
		//Network error?
		if (xhr.status==0) {
			if(thrownError)
				readable = thrownError;
			else readable = xhr.statusText;
		//Wrong auth?
		} else if (xhr.status==401) { 
		
			//New login box
			display_login("Invalid login specified.");
			
			//If it was called with authajax, try to do again
			if (ajaxOptions.__myData)
				authQueue.push(ajaxOptions);
			
			
			return;
		
		//Other error
		} else {
			readable = xhr.status+" "+xhr.statusText;
			if (xhr.statusText!=thrownError)
				readable+=" "+thrownError;
		}
		alert(readable);
	});
}

var root = findRoot(); //Root path to server
var username = window.localStorage[clientName+"username"]; //Auth username
var password = window.localStorage[clientName+"password"]; //Auth password
var loggedin = false; //We don't know if the stored data is valid yet.
if (username && password) {
	console.log(username);
	console.log(password);
} 


var partials;

var currentDisplay = null; // Currently displayed element.
var authQueue = []; //Queue of requests to make when auth finishes.


var resources = {};
var resourceQueue = {};
var resourceRaw = {};
var resourceRawQueue = {};
/**
 * Get some unchanging resource, only once per page load and return the cached result for subsequent requests
 */
function getResource(name, url, callback, transform) {
	//Already loaded?
	if (resources[name]) {
		callback(resources[name]);
		return;
	}
	//Currently loading with same name?
	if (resourceQueue[name]) {
		resourceQueue[name].push(callback);
		return;
	}
	//Already loaded url?
	if (resourceRaw[url]) {
		res = resourceRaw[url];
		if (transform)
			res = transform(res);
		resources[name] = res;
		callback(res);
		return;
	}
	//Currently loading url with different name?
	if (resourceRawQueue[url]) {
		//Call self when finished.
		resourceRawQueue[url].push(function() {
			getResource(name,url,callback,transform);
		});
		return;
	}
	
	console.log("resource requested: "+url +" as "+name);
	//Else start new request.
	resourceQueue[name] = [callback];
	resourceRawQueue[url] = [];
	authajax({
		'url':url,
		'dataType': 'json'		
	},function(res) {

		resourceRaw[url] = res;

		if (transform) res = transform(res);
		resources[name] = res;
		
		queue = resourceQueue[name];
		delete resourceQueue[name];
		
		
		queue.forEach(function(elem) {
			elem(res);
		});
		

		queue = resourceRawQueue[url];
		delete resourceRawQueue[url];
		
		queue.forEach(function(elem) {
			elem(res);
		});
		
	})
}

function invalidateResources() {
	//TODO: What if we are loading resources right now?
	console.log("Resources invalidated.");
	resources = {};
	resourceQueue = {};
	resourceRaw = {};
	resourceRawQueue = {};
}

function getUsers(callback) {
	getResource("users",root+"rallye/users",callback, function (arr) {
		var res = {};
		arr.forEach(function (elem) {
			res[elem.userID] = elem;
		});
		return res;
	});
}


function getGroupsAsArray(callback) {
	var url = root+"rallye/groups";
	if (clientName=="admin")
		url = url+"/admin";
	getResource("groupsArray",url,callback, function (arr) {
		return arr;
	});
}

function getGroups(callback) {
	var url = root+"rallye/groups";
	if (clientName=="admin")
		url = url+"/admin";
	getResource("groups",url,callback, function (arr) {
		var res = {};
		arr.forEach(function (elem) {
			res[elem.groupID] = elem;
		});
		return res;
	});
}


function getTasksAsArray(callback) {
	getResource("tasksArray",root+"rallye/tasks",callback, function (arr) {
		return arr;
	});
}

function getTasks(callback) {
	getResource("tasks",root+"rallye/tasks",callback, function (arr) {
		var res = {};
		arr.forEach(function (elem) {
			res[elem.taskID] = elem;
		});
		return res;
	});
}
/**
 * wrapper for jQuery.ajax, adding auth information
 */
function make_auth_request(q) {
	console.log("making authed request for "+q.url);
	q.username = username;
	q.password = password;
	$.ajax(q)
		.done(q.__myData.done)
		.error(q.__myData.fail)
		.always(q.__myData.always);
}

function display_login(msg) {
	if (!msg) msg = "Please log in.";
	console.log("Displaying login box");
	
	var template = $('#templ_logindialog').html();
	var html = Mustache.to_html(template,msg);
	
	$("#main").html(html);

}

function submit_login() {
	console.log("submitting login");
	username = $("#username").val();
	password = $("#password").val();
	
	if (!username || !password) {
		display_login("Please enter username and password.");
		return;
	}
	
	var remember = $("#remember").is(':checked');
	console.log($("#remember"));
	
	console.log("new auth: "+username+" "+password);

	if (remember) {
	 	$("#logout").css("display","");
		window.localStorage[clientName+"username"] = username;
		window.localStorage[clientName+"password"] = password;
	}
	
	$("#main").html("Logging in...");
	
	
	send_socket_login();
	
	return false;
}

/**
 * Checks if auth info has been supplied, makes an jQuery ajax request if yes, displays auth if not
 */
function authajax(obj, done, fail, always) {

	console.log("making request for "+obj.url);
	obj.__myData = {
		done: done,
		fail: fail,
		always: always
	};
			

	//Is there auth info?
	if (!loggedin) {
		console.log("but no auth specified");
	
		//Are we already displaying auth?
		if (currentDisplay != "auth") {
			console.log("therefore showing login");
			display_login();	
		} else 
			console.log("already showing login");
		//Queue request
		authQueue.push(obj);
	
	} else
		make_auth_request(obj);

}

function kill_auth() {
	console.log("killing auth");
	username = null;
	password = null;
	window.localStorage.clear();
	window.location.reload()
	return false;
}



function get_system_info(callback) {
	$.ajax({
		url: root+"rallye/system/info",
		dataType: "json",
	}).done(callback);
}


function leadZero(num) {
	if (num<10)
		return '0'+num;
	return num;
}
function formatTime(unixTimestamp) {
    var dt = new Date(unixTimestamp * 1000);

	var day = leadZero(dt.getDate());
	var month = leadZero(dt.getMonth()+1);
	var year = dt.getFullYear();
	
    var hours = leadZero(dt.getHours());
    var minutes = leadZero(dt.getMinutes());
    var seconds = leadZero(dt.getSeconds());


    return day+"."+month+"."+year+" "+hours + ":" + minutes + ":" + seconds;
}       

function get_nodes(callback) {
	$.ajax({
		url: root+"rallye/map/nodes",
		dataType: "json"
	}).done(callback);
}

var urlParams;
function load_querystring() {
	var match,
	pl     = /\+/g,  // Regex for replacing addition symbol with a space
	search = /([^&=]+)=?([^&]*)/g,
	decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
	query  = window.location.search.substring(1);
	
	urlParams = {};
	while (match = search.exec(query))
		urlParams[decode(match[1])] = decode(match[2]);
}
function getParameterByName(name) {
    return urlParams[name];
}
//Sends a notification that expires after a timeout. If timeout = 0 it does not expire
function sendNotification(image, title, message, timeout, showOnFocus) {
  // Default values for optional params
  timeout = (typeof timeout !== 'undefined') ? timeout : 0;
  showOnFocus = (typeof showOnFocus !== 'undefined') ? showOnFocus : true;

  // Check if the browser window is focused
  var isWindowFocused = document.querySelector(":focus") === null ? false : true;

  // Check if we should send the notification based on the showOnFocus parameter
  var shouldNotify = !isWindowFocused || isWindowFocused && showOnFocus;

  if (window.webkitNotifications && shouldNotify) {
    // Create the notification object
    var notification = window.webkitNotifications.createNotification(
      image, title, message
    );

    // Display the notification
    notification.show();

    if (timeout > 0) {
      // Hide the notification after the timeout
      setTimeout(function(){
        notification.cancel()
      }, timeout);
    }
  }
};
function setupNotifications() {
	if (window.webkitNotifications.checkPermission() != 0) { // 0 is PERMISSION_ALLOWED
	    window.webkitNotifications.requestPermission();
	}
	return false;
}
//Provide file both as mp3 and ogg for different browsers, specify it here without file extension.
function playSound(filename){   
    document.getElementById("sound").innerHTML='<audio autoplay="autoplay"><source src="' + filename + '.mp3" type="audio/mpeg" /><source src="' + filename + '.ogg" type="audio/ogg" /><embed hidden="true" autostart="true" loop="false" src="' + filename +'.mp3" /></audio>';
}

var connectRetries = 0;
//Reconnect to websocket after some time, increasing with number of retries
function socket_reconnect() {
	var time = connectRetries*connectRetries; // wait number of retries squared in seconds


	var status = $("#status");
	status.text("Connection lost, retrying after "+time+"s");

	window.setTimeout(function() {
		setup_socket();
	},time*1000);

	connectRetries++;
}