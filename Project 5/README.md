
# 3700-4

<pre><b> 
High-level Approach: </b>
  
We began this project by reading the project description to understand the requirements. We also looked 
at the starter code to understand the structure and functionality. We then asked a TA about any initial 
questions we had before beginning planning our program. For our program, we decided to use Python since 
we are most familiar with it. We decided to map out our approach in pseudocode first, before fully 
implementing it.

We agreed that our web crawler program needed to have the functionality to:
    1. parse the command line input
    2. establish and maintain a connection to a specific server on a specific port, where the server and 
       port is set as "proj5.3700.network" and 443 respectively as the default unless otherwise specified 
       in the command line 
    3. be able to send HTTP requests and receive and handle HTTP responses to/from a 
       specific server on a specific port 
    4. be able login into Fakebook (based on the username and password specified in the command line) 
       through a GET and POST request and handle their accompanying responses
    5. traverse all pages in the Fakebook site and inspect them for flags via GET requests and their 
       responses without accessing the same page more than once via GET requests and their responses

Within these overarching functionalities (described above), we then considered more specific details 
such as:
- the format of the GET and POST request for login and the GET request to access pages after logging in
- the ability to retrieve the csrftoken, sessionid, and/or csrfmiddlewaretoken from a given HTTP response 
  and update it
    - ensure the sessionid is up to date in each GET request
- the ability to handle responses with 200, 302, 403, 404, and 503 status codes by modifying and retrying 
  the request

After understanding the functionalities needed to be implemented, we played around with the Dev Tools
to understand HTTP request and HTTP response formats, and take note of how cookies are managed.

When implementing the functionality of the program, we use a TCP socket wrapped in TLS and maintain 
a connection to a specific server on a specific port using keep-alive. Thus, we handle remaking the 
connection when broken. We create a GET and POST request to log in properly into Fakebook. We maintain 
variables for the token, session_id, middlewaretoken so they can be sent as needed with each request. 
We update them everytime we read a HTTP response. After successfully sending the POST request, we then 
retrieve the homepage of Fakebook (seen after logging in). This pages gives a list of profiles we can 
begin to inspect for flags. We decided to use a breadth-first search algorithm to traverse the pages 
in Fakebook efficiently. Thus, in our code, we maintain two global array data structures: 
profiles_to_inspect and profiles_already_added. profiles_to_inspect contains URLs of pages that need 
to be inspected for flags. The first item in the array is inspected and then popped. profiles_already_added 
maintains an ongoing list of pages already "seen" by our program to ensure a page is not visited twice. 
Flags are stored in a third global array called flags. We send GET requests for each page we want to 
inspect, by specifying the URL of the page we are trying to request. When receiving responses, we receive 
responses until the end of the header if the content-length is 0 or until the closing html tag if it is 
not 0. We parse the responses. Our program prints the five flags, as they are found, and exits when all 
five are found. We also check and handle the response status codes through an if/elif structure that 
checks for the status code and reformats and/or resends the request accordingly, or abandons it entirely. 

We ran the code and ensured that all flags could be found. When we confirmed this was true, we focused on 
efficiency. Our program was far slower than the 30 minute limit at first, so we first changed how we were 
parsing responses. Instead of decoding a response, splitting it on spaces, then iterating over it to 
parse the response and extract the necessary information, we decided to use re.findall() and substring 
functionality. This made some difference but we knew this was not enough to combat the slowness. So, we 
decided to implement "connection: keep-alive" functionality so a socket does not have to be opened and 
closed over and over again. Instead, it only needs to be reopened when the server closes it. We also 
implemented threading to run 5 parallel requests, each independent of the other in terms of the 
connection to the server. We made sure to lock where two threads should not access/modify the same 
thing. 

We continued to test and debug our program throughout the development and finished by also writing this 
README.md and creating the secret_flags file.


</pre><pre><b>Challenges Faced:</b><br>

There is one main part of the program we struggled with - logging in. It is important to note that we 
included the GET request to the Fakebook home page as part of logging in as it was a step we had to 
complete before running the crawling of the profiles and their friend pages. Under that category, there 
were three separate issues we encountered. 

The first is recreating a connection with the server after the initial request to the 3700 Crawler 
page. At first, we were getting "b''" response after our POST request and figured the header was 
improperly formatted. Once we changed the header to have a connection be set to "keep alive" we were 
finally able to see what our POST request was actually returning. We did try another method first 
that involved closing and recreating a connection with the port/socket before sending a request. This 
was later scrapped for a cleaner solution.

The second obstacle we faced was correctly dealing with the 302 code response. Initially, we thought we
needed to update the URL and send another POST request to try and logging in again. Once speaking to a TA 
where we were told to send a GET request instead. After making that edit our program showed signs of a 
different error (error number 3), but properly getting past the login page was complete.

The third issue was not updating our session ID for the following GET requests. Debugging this error 
was challenging since it resulted in us getting caught in a loop of being redirected to three different 
pages (root, fakebook, login page). The cause was not obvious to us and took us to review Piazza posts 
to realize we were not adding the updated session ID to our requests. Once we did that only one 302 code 
response was sent back and our program was able to get to the fakebook home page. 

Once the logic of the crawler was complete the next significant issue was meeting the runtime limit. 
Locally it would run under 30 mins but when submitted to Gradescope it took longer and timed out. Due
to the fact we had already done what most TAs and the professor we finally tried implementing 
multithreads (5) to make our program run faster. When doing so a small bug of not printing the flags
occurred. 

</pre><pre><b>Testing:</b>

In this project, our primary method of testing code involved utilizing print statements to see if we were 
getting stuck in an infinite loop (by not properly keeping track of the frontier) or if there were issues 
with dealing with error codes. We also used print statements to ensure we were parsing URLs, flags, tokens,
etc. properly. When debugging logging in and dealing with the 302 code we printed out the requests and the 
data that was sent in response. Doing so allowed us to see what page we were being redirected to (location
value) and if we ended up on the correct page after logging in. Due to this project being a bit simpler 
with the communication between the program and server being more straightforward, we did not do much else 
to debug our code. We did, however, run the program on both our local environment and the Khory VM to ensure 
that we got the same result and runtime. 

</pre>
