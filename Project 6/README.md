# 3700-6

<pre><b> High-level Approach: </b>

We began the implementation process for this program by thoroughly understanding the RAFT Protocol.
We read the "RAFT Paper" and discussed our doubts with the Professor and TAs. Then, we implemented basic
functionality, such as maintaining stable elections, before moving on to implementing maintaining logs,
and improving performance. We continuously tested our program during the entire implementation process. 
We decided to use Python, as it was the language we are both most familiar and comfortable with.

Our program functions to receive and send messages through the port to fulfill client get and put
requests and maintain consistency among all replicates. The program starts with all replicas sending a 
hello message and conducting the first election to choose the first leader within a 5 second timespan 
(more details will be provided later). Once the 5 seconds are up, clients start sending them requests. 
The next paragraphs will detail all the individual moving parts of the program, including handling 
clients, conducting elections, and the continuous background maintenance.

Handling Clients:
  If a client sends a request, there are three responses that can be given from a leader or a 
  follower.
    - In the case where the request is directly sent to the active leader, once a commit quorum is 
      met, the leader can send an "ok" message.
    - In the case the request is sent to everyone (aka "FFFF") and there is an active leader, a 
      follower can send a "redirect" message that informs who the current leader is.
    - In the case there is no leader and a request is sent, a "fail" message will be sent by a replica
      to tell the client to try again. This is in the hopes that a new leader will be elected to respond. 
  To reduce the number of messages sent between between the leader and replicas, we implemented batching 
  which adds the requests into an array that will be sent to all followers to update a "batch" of entries 
  each interval. This can slow down the latency for clients if messages are dropped. However, the delay 
  should not be so long that it affects this measure.

Handling Elections:
  Each replica maintains a timeout interval, chosen between 0.2 and 0.6 seconds, at random.
  If there is no replica sending append entry messages, the replica that times out first will start an
  election by sending a request message to every other replica. If a replica receives a request message
  it will first check to see if they have already voted in a given term. Then they check if the 
  candidate's last index and election term is the same or larger than its own and if the last entry is 
  the same if they share the same last index. If they meet all requirements, the follower will vote for 
  the candidate by sending a in-favor voting message. If the follower does not vote for the candidate, 
  they have not voted during this election. Multiple elections can happen at the same time. So, if the 
  candidate does not get majority vote, another replica may be chosen via the same requirements. To 
  prevent a candidate from starting another election during the same term, they reset their timeout 
  timer. 

Continuous Background Maintenance:
  are not relating to elections and directly interacting with clients. While there are no new entries
  to commit to the log and database, there needs to be some way for the leader to tell the followers it
  is still active and to not start an election. It does this in the form of an empty append 
  entry/heartbeat message. This message doubles as a way to keep follower's logs updated by continuously 
  comparing both replicas' logs (last index and last entry). If the follower's values are not equal or 
  less than the leader's, it sends a "commit" message with the quorum field being false to the leader. 
  The leader then sends a message up to 50 entries to the follower to update their logs and database. 
  Once another heartbeat message is sent the same comparison is made and the cycle will continue until 
  the follower is caught up. The follower will then no longer respond to heartbeat messages and only to 
  append entry ones. The append entry messages are meant to add new entries to keep everyone updated with 
  the most recent client transaction. To confirm a commit, there needs to be a quorum made up of a 
  majority of the followers to do so. If a commit cannot be agreed upon, more commit messages will be 
  sent from the followers, as mentioned before, and the leader will attempt again until a commit could 
  be made. Again, we have implemented batching to reduce the number of messages being sent between 
  replicas with 50 entry long messages. 
  
Once the 35 second mark is hit, clients will no longer send requests and the program comes to an end.   

</pre><pre><b>List of Notable Properties/Features:</b>

There are select notable properties/features in our implementation.

  First, our code clearly handles the different types of messages sent between replicas and to clients. 
  Our run function is organized by the type of each message. Each type of message is handled by logical 
  functions designed to handle that  type of message specifically. Some examples of clearly outlined 
  functions for specific message types include redirect_response, fail_response, and ok_response. There 
  are also situations specific functions, such as conduct_election, that are not necessarily associated 
  with one type message but handle a particular procedure within our larger program. Ultimately, this 
  makes our code easy to follow and debug. 

  Another notable feature our effective use of batching for sending append entry and commit messages. 
  The use of batching ensures that multiple log entries can be sent to replicas in one message. We ensure, 
  that after each message involving batching is sent, a response is received/the replica's commit index 
  increased. This ensures the replicas that are not fully updated can be updated (if necessary) and a 
  commit can occur. This feature ensures that leaders do not send excessive amounts of messages to other 
  replicas.

  Building on the point above, we resend failed commits and append entry messages. This extra measure 
  makes sure that messages containing log information are always received by the intended replicas. 
  This property also ensures that the replicas logs can be updated if they are not already up-to-date 
  and thus, every replica maintains an up-to-date log and database. We ddo this by sending regular 
  append entries and adding new entries piece by piece.

</pre><pre><b>Challenges Faced:</b>

We faced a few different challenges throughout our implementation process of this program.

  One of the challenges we encountered while testing was that we sending messages that were far too long 
  and thus, resulting in "Message too long" errors. These messages were primarily append entry and commit 
  type messages, as they are both responsible for ultimately ensuring replicas' logs and database are 
  up-to-date. Since the number of entries sent in each message was too large, we implemented batching. This 
  ensured no more then 50 entries are in each message and while there are more entries to send, the program 
  will continuously send these messages one after the other. While this eliminated the "Message too long" 
  errors, we quickly realized that there was a high chance some of these packets could get dropped, 
  especially during the unreliable tests, and there would be no way to recover them, since the messages 
  were sent one after the other. This would reduce consistency of logs/database among all the replicas. 
  The solution to this was to send 50 entries at a time, and between each message sent, wait for a 
  response/check in on whether the replica's commit index increased. This way, there is more assurance that 
  a message reached its intended destination and the proper updates/commits occurred and a way to retry if 
  this did not occur.
  
  Another challenge we faced was that our replicas were detecting timeouts far more frequently than they 
  should. Specifically, despite there being a leader alive, functioning, and sending append entry heartbeats, 
  replicas were timing out and initiating unnecessary elections. Upon a closer look at our replicas' timeout 
  range and the heartbeat interval we had set, we realized that our heartbeats were not always reaching the 
  other replicas before they timed out. To ensure that the heartbeats were reaching the other replicas in a 
  timely fashion, we decreased our heartbeat interval to be a at least .05 seconds smaller than the starting 
  value of our timeout range. 

  Another challenge we encountered was that leaders were sending too many unnecessary append entry heartbeats. 
  This increased the number of messages being sent between replicas by a large amount. Specifically, while it 
  is crucial for a leader to send append entry heartbeats at some time interval, our program was resulting in 
  leaders sending more heartbeats than just one after each heartbeat interval. After debugging by analyzing 
  timestamps of the heartbeat messages, and trying different heartbeat intervals, we discovered that we were 
  not resetting the heartbeat timestamp (i.e. when we sent the last heartbeat), when a leader sends an append 
  entry message. After adding self.heartbeat_timestamp = time.time() in our send_append_entry function, we 
  resolved this problem.

</pre><pre><b>Testing:</b>

In this project, our primary method of testing code involved utilizing print statements, paying 
attention to timestamps and states of certain variables for debugging purposes after executing the 
provided configuration test files. This was particularly helpful for finding bugs in our elections 
as there are many moving parts and it was a challenging portion of the program to fix. To address 
the errors stemming from inconsistent logs among replicas, which will cause election issues, we began 
printing the logs of each replica to a file labeled with its ID. This helped us check to see where 
entries become disorganized or lost. Simply printing these out in the terminal would lead to abrupt 
line cutoffs and interruptions which made it impossible to compare the logs. We also executed the code 
on both our local environments and on the Khoury VM to ensure performance across different 
setups/machines remain consistent.
</pre>
