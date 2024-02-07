# 3700-Project-2
<pre>
High-level Approach:

For this project I wanted to keep all the code in one file to reduce the likelyhood of making 
mistakes around handeling and passing the sockets with data around. I start off by connecting to 
the control socket and then reading the welcome messaging to ensure it is the correct server. Then
I parse the username and password from the url (if there is one) and login to the server. After
that I send the TYPE, MODE, and STRU commands to setup the server for the data channel. All the 
setting up has been done for both groups of commands (ones that need the data channel and ones 
that don't) so I parse the path out of the url depending on the number of parameters and which 
ones contains the "ftp.example". Next I group the main actual functionaility of the program into 
an if else statement. The if checks if the command is "ls", "cp", or "mv", if it is the "PASV"
command is sent to the ftp server and the response is saved into a string that is used to parse
the IP address and port number for the data channel. Once connected a helper method is called 
to do the actualt heavy lifting which then prints out the control channel response and closes the 
data socket once the task is complete. If the command is "rm", "rmdir", or "mkdir" a command is 
sent the control channel and the response is printed. Once the program gets pasted the if statement
the "QUIT" command is sent, the "GoodBye" message is printed, and then the control channel is closed.
</pre> <pre>
Challenges:

Understanding how the program should work and how the control/data channels worked together was 
easy to understand. I struggled the most connecting to the data channel as I made an assumption 
that was incorrect and it took me a while to realize why the connection was not working. The 
second would be understanding what was needed for the cp and mv commands since both the params
and logic was a bit unclear to me.
</pre><pre>
Testing: 

The three main tools I used to test my code is reading the error messages from both the server and 
computer, using print statements to pin point the line that was causing issues and what values of 
variables throughout the program, and TAs. I needed to help from the TAs the most for my Makefile 
and figuring out with input and output streams would work on the ftp server.
</pre>
