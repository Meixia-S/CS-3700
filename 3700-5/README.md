
# 3700-4

<pre><b> 
High-level Approach: </b>

</pre><pre><b>Challenges Faced:</b><br>

There is one main part of the program we struggled with - logging in. It is important to note that we 
included the GET request to the Fakebook home page as part of logging in as it was a step we had to 
complete before running the crawling of the profiles and their friend pages. Under that category there 
where three separate issues we encountered. 

The first being recreating a connection with the server after the initial request to the 3700 Crawler 
page. At first, we where getting "b''" response after our POST request and figured the header was 
improperly formatted. Once we changed the header to have connection be set to "keep alive" we were 
finally able to see what our POST request was actually returning. We did try another method first 
that involved closing and recreating a connection with the port/socket before sending a request. This 
was later scrapped for a cleaner solution.

The second obstacle we faced was correctly dealing with the 302 code response. Initially we thought we
needed to update the URL and send another POST request to try and logging again. Once speaking to a TA 
where we told to send a GET request instead. After making that edit our program showed signs of a 
different error (error number 3), but properly getting past the login page was complete.

The third issues was not updating our session id for the following GET requests. Debugging this error 
was challenging since it resulted in us getting caught in a loop of being redirected to three different 
pages (root, fakebook, login page). The cause was not obvious to us and took us reviewing Piazza posts 
to realize we were not adding the updated session id to our requests. Once we did that only one 302 code 
response was sent back and our program was able to get to the fakebook home page. 

Once the logic of the crawler was complete the next significate issue was meeting the runtime limit. 
Locally it would run under 30 mins but when submitted to gradescope it took longer and timedout. Due
to the fact we had already done what most TAs and the professor we finally tried implementing 
multithreads (5) to make our prgram run faster. When doing so a small bug of not printing the flags
occured. 

</pre><pre><b>Testing:</b>

In this project, our primary method of testing code involved utilizing print statements to see if we were 
getting stuck in an infinite loop (by not properly keeping track of the frontier) or if there where issues 
with dealing with error codes. We also used print statements to ensure we were parsing URLs, flags, tokens,
etc. properly. When debugging logging in and dealing with the 302 code we printed out the requests and the 
data that was sent in response. Doing so allowed us to see what page we were being redirected to (location
value) and if we ended up in the correct page after logging in. Due to this project being a bit simpler 
with the communication between the program and server being more straight forward we did not do much else 
to debug our code. We did, however, run the program on both our local environment and the Khory VM to ensure 
that we got the same result and runtime. 

</pre>

// Link to repo for better formatting: https://github.khoury.northeastern.edu/sindelar/3700-5/
