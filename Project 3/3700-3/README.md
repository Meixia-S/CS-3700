# 3700-3
<pre>
High-level Approach:

For this project, my partner and I began by reading the project requirements. We met a few times to 
understand the different parts of the project and what the ultimate goal was. We also spent time 
reading through and understanding the starter code so that we could use it effectively. We went to 
office hours to get an understanding of the `select()` and `poll()` functions as well, because we were 
very unfamiliar with them and their use cases. With basic knowledge of the project, we decided to look 
through the config files and understand how the tests were structured.

Once we had a basic understanding of the project, starter code, and tests, we decided our programming 
language. Although my partner had never used Python, we decided that Python was the simplest and most 
convenient language to use for this project. Now, we were ready to start coding. We decided to approach 
the assignment in small sections, handling one or two features at a time. Having looked at the started 
code, My partner and I knew that it handled opening domain sockets, listening to them via `select()`, 
and sending the handshake message. We added additional lists called `cust` and `peer_prov_cust` to make 
determining recipients of messages more convenient. So, the next step was to start tackling all the 
different types of messages we could receive. Given the run function in the starter code, that was 
handling receiving data from the `recv()` function, we knew this function was the primary place to 
determine the type of message and handle that type accordingly. To make parsing through the messages 
more convenient, after calling `msg_json = json.loads(k)`, we broke the message down into the 4 primary 
parts: src, dst, type, and msg and store this as an Entry object, which `Entry` is a class that we 
created. The `Entry` was a design choice we made to also make it easy to get all the possible components 
of a message such as the network, netmask, localpref, selfOrigin, ASPath, origin, and peer. From run, we 
were able to determine the type of message and then call one of the following functions: 
`handle_update_msg`, `handle_withdraw_msg`, `handle_dump_msg`, or `handle_data_msg`. We generally took 
the suggested implementation approach and implemented the easier features first, that would pass the 
lower level tests, before handling the harder features. Specifically, we created basic implementations 
for update and data messages, followed by dump messages. We created `storage_announcement` list to be a 
cache of announcements as well as a `routing_table` list to represent the routing table. We then handled 
the other scenarios for data messages, involving longest prefix matching and tie breaking. Next, we 
tackled withdraw messages, which we decided to store in the list `withdraw_storage` and sending no route 
messages as needed. Finally, we implemented a legality check to check relationships between sources and 
destinations and aggregation/disaggregation functionality. Between implementing each of these 
functionalities, we ran the corresponding tests and debugged using print statements. We made sure each 
level of tests passed before moving on to the next level, as many of the later tests depended on 
functionalities implemented for the earlier tests. 

Once we completed implementing our BGP router, we abstracted and cleaned our code, tested everything one 
more time by running the config files, and completed this README.

</pre>
<pre>Challenges Faced:</pre>

During this project we faced main issues related to correctly sending "no route" messages, calculating 
longest prefix match, and disaggregation.

Sending "no route" messages
  - The indication that we were not correctly sending the message to the correct src address was continuously failing test 3-1.
  - The solution was to use the srcif port number instead of the src given within the message.
  
Longest prefix match 
  - The indication that our longest prefix match functionality was buggy was failing test 5-1 and 5-2.
  - We first tried a brute force method to pass the milestone but then edited our code to use the bitwise and operation. The 
    transition of our code lead to some unnecessary functions that were manipulating our data and passing in the wrong matches 
    to break the tie. 
      - To solve this, we went through our code and printed out all the arguments that where being passed into the functions.
  - We also had too many for loops that were traversing the possible_recipients array twice in the function. Thereby not 
    correctly letting us to pass which was another reason why our data was inaccurate and messing up the break the tie outcomes.
      - The solution was to get rid out the outer for loop and simple traverse the possible_recipients while traversing the 
        possible_recipients_and_result array which would always be the same or short then it.

Disaggregation
  - The indication that we were not correctly disaggregation the routing table was continuously failing test 6-3.
  - We tried a couple different methods but then settled on relying on aggregating the table after we "undid" it and removed 
    the given entry from the routing table. This was a cleaner way and the one suggested on the project description. 
  - Running into concurrent modification errors when it came to traversing the announcement_storage array and removing entries 
    while doing so also came up. We had run into this error in other parts of our code during the process but this the last
    time we had to deal with it. The solution was simply appending the entries to a separate array.
<pre>List of Notable Properties/Features</pre>

  - Decided to represent the messages as Entry class objects
    - This allowed us to easily access the src, dst, type, msg, and all the msg contents easily without having to 
      preform another operations, except a simple function call.
      
  - The main method run() was concise and easy to understand 
    - We simply edited the pre-existing function and added 13 lines that...
      - Parsed the JSON object
      - Created an Entry object with the parsed JSON
      - Checked to see which type of message it was and call the helper functions that preformed the functionality
      
  - Organized our tie breaking functionality into 5 distinct functions
    - Each function handled one comparison and returned a filtered array of the possible recipients. 
    
  - Helpful global arrays that kept track of routing table entries, all announcements, and withdraw requests 
      (self.routing_table, self.announcements_storage, and self.withdraw_stroage repectively)
    - These arrays helped us preform the aggregation and disaggregation as they held all the information we needed. 
    - It was also helpful to have an easy way to print all the entries in a particular category to debug.   
    
  - In general, the code is organized in an orderly fashion that makes it easy to read and understand what each function 
    did and how they all fit together.
    - Our comments also helped in our efforts to make our code easily readable.
<pre>Testing: 

    In our past projects, our primary method of testing code involved utilizing print statements for 
    debugging purposes after executing the provided configuration test files. We placed significant 
    reliance on the feedback generated during test runs, particularly focusing on error codes. By 
    meticulously comparing these messages with the outputs of our data representations (such as 
    self.routing_tables and self.announcement_storage), we were able to identify bugs within our code 
    and uncovering previously overlooked edge cases. It's worth noting that we executed the code on both 
    our local environments and on the Khoury VM to ensure consistent performance across different setups.
    add a line about try excepts to catch errors for sending and receving messages.
</pre>

// Link to repo for better formatting: https://github.khoury.northeastern.edu/sindelar/3700-3
