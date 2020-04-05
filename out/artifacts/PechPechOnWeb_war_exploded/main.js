var sessionId = '';

var username = '',password = '';

var socket_url = "parsa.boshrapardaz.ir";
// var socket_url = "localhost:9090";
var port = 9090;

var webSocket;

$(document).ready(
    function()
    {
        $("#form_submit, #form_send_message").submit(
            function(e)
            {
                e.preventDefault();
                join();
            }
        );
    }
);

function join()
{
    if( $("#input_username").val().trim().length <= 0 )
    {
        alert("enter your username");
    }
    else if( $("#input_password").val().trim().length <= 0 )
    {
        alert("enter your password");
    }
    else
    {
        username = $("#input_username").val().trim();
        password = $("#input_password").val().trim();

        $("#prompt_name_container").fadeOut(1000,
            function()
            {
                openSocket();
            }
        );
    }

    return false;
}

function openSocket()
{
    if( webSocket !== undefined && webSocket.readyState !== webSocket.CLOSED )
    {
        return;
    }

    webSocket = new WebSocket(
        "wss://" + socket_url + "/PechPechOnWeb/chat?username=" + username
        +"&password="+ password
    );

    webSocket.onopen = function(event)
    {
        $("#message_container").fadeIn();

        if( event.data === undefined )
        {
            return;
        }
    };

    webSocket.onmessage = function(event)
    {
        parseMessage( event.data );
    };

    webSocket.onclose = function(event)
    {
        alert("Error! Connection is closed! Try again later...");
    };
}

function parseMessage(message)
{
    var jObj = $.parseJSON( message );

    /*
     *  flags: self , new , exit , message
     *
     */

    if( jObj.flag == "self" )
    {
        sessionId = jObj.sessionId;
    }
    else if( jObj.flag == "new" )
    {
        var new_name = "You";

        var online_count = jObj.onlineCount;

        $("p.online_count").html(
            "Hello, <span class='green'>" + username + " </span>. " +
            "<b>" + online_count + "</b> peoples are online right now!"
        ).fadeIn();

        if( jObj.sessionId != sessionId )
        {
            new_name = jObj.name;
        }

        var li = "<li class='new'>" +
                    "<span class='name'>" + new_name + " </span>" + jObj.message +
                 "</li>";

        $("#messages").append(li);

        $("#input_message").val('');
    }
    else if( jObj.flag == "exit" )
    {
        var li = "<li class='exit'>" +
                    "<span class='name red'> " + jObj.name + " </span> " + jObj.message +
                 "</li>";

        var online_count = jObj.onlineCount;

        $("p.online_count").html(
            "Hello, <span class='green'>" + username + " </span>. " +
            "<b>" + online_count + "</b> peoples are online right now!"
        );

        appendChatMessage( li );
    }
    else if( jObj.flag == "message" )
    {
        var from_name = "You";

        if( jObj.sessionId != sessionId )
        {
            from_name = jObj.name;
        }

        var li = "<li>" +
                    "<span class='name'>" + from_name + " </span>" + jObj.message +
                 "</li>";

        appendChatMessage( li );

        $("#input_message").val('');
    }
}

function appendChatMessage( li )
{
    $("#messages").append( li );

    $("#messages").scrollTop(
        $("#messages").height()
    );
}

function sendMessageToServer(flag , message)
{
    var json = '{""}';

    var myObj = new Object();

    myObj.sessionId = sessionId;
    myObj.message = message;
    myObj.flag = flag;

    json = JSON.stringify( myObj );

    webSocket.send( json );
}

function send()
{
    var msg = $("#input_message").val();

    if( msg.trim().length > 0 )
    {
        sendMessageToServer("message" , msg);
    }
    else
    {
        alert("Please enter your message...");
    }
}

function closeSocket()
{
    webSocket.close();

    $("#message_container").fadeOut(3000,
        function()
        {
            $("#prompt_name_container").fadeIn();

            sessionId = '';
            username = '';
            password='';

            $("#messages").html('');

            $("p.online_count").hide();
        }
    );

}