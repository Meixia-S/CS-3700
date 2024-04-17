
# 3700-4

<pre><b> 
High-level Approach: </b>

Our reliable TCP program needed to support 5 different functionalities:
    1) receive data from the simulator, turning it into a packet that holds more information, and send 
       it to the Receiver (packet number and a unique checksum value)
    2) receive the Sender's packet, check for corruption and indicating if it is to the Sender, and ack 
       the packets in ascending order
    3) Sender must detect if packets have been dropped with a dynamic RTT implementation and resend 
       the dropped message to the Receiver
    4) once all the packets have been sent, the program will end by closing all ports/connections
    5) this all happens at the pace of the sliding window, with the ss_threshold being set to 10
 
The program starts with receiving data from the simulator and creates a packet that has the key values of 
"data", "pk_ num", and "checksum" that represent the actual content of the message, its order within all 
the packets, and its unique checksum value that will be used to check weather it has been corrupted 
through its transmission to the Receiver (checksum is created using only the first three fields), 
respectively. Once the message has been created the Sender sends it to the Receiver. 

The Receiver then checks to see if this packet is a duplicate:
    - If it is then it is checked that it is currently in the queue to be acked and whether it is 
      corrupted. 
        - If it is corrupted, an Ack message will NOT be sent to the Sender.
        - If it is NOT corrupted, an Ack message will be sent to the Sender with a checksum.
          
    - If it is NOT a duplicate packet, we want to add it to the list of received messages and check 
      whether it is corrupted. 
        - If it is a duplicate, an ack message will NOT be sent to the Sender. 
        - If it is NOT a duplicate, we add it to the list of messages we have received, add it to the 
          queue to be acked, and a call to a helper to ensures the messages are acked in ascending order.
            - Once a message is ready to be acked, we create a checksum for it.
  
The Sender is perpetually waiting for either an Ack msg or for data to create and send a new message:
    - If the waiting flag is set to True we will only be expecting a given number ack msg that is
      managed by an if statement (self.congestion_window <= len(self.msgs_not_acked)).
            - This is how we implement the sliding window on the Sender side.
           
    - Once an Ack msg is received, a few checks are done. This includes a corruption check of the ack, a 
      duplicate check, and a check fo if the ack was expected:
            - If it passes the 3 checks, we log the ack, add it to the list of received packets, remove 
              the corresponding message from the list that keeps track of the packets that hav NOT been 
              acked, set waiting to False, grow the window, and calculate the RTT.
            - If it does NOT pass the checks, we do nothing. 
      
    -  If the waiting flag is set to False we are not expecting more data to be sent. So, we simply 
       repeat the process of creating a new message and sending it to the Receiver, as long as there
       is data to be received from the simulator.
    
 After all connections in the socks array have been iterated over, the Sender checks for 
 dropped packets.

    - It does this by checking if the time passed since the message was sent is greater than the timeout 
      limit value (new_rtt * 2).
        - If it is we want to resend the packet, update the timestamp associated with this message, and 
          shrink the window.
    - If it is NOT, we simply restart the connections in socks for loop (redo the steps discussed above).
    
Once there are no more data being sent to the Sender and all the acks have been received, the program 
ends all ports/connections with both the simulator and Receiver.
</pre><pre><b>List of Notable Properties/Features:</b>

There are some properties in our code that are worthy to note for their creativity and/or usefulness.

    - To check for packet corruption, we implement a checksum for both acks and messages via the md5 algorithm in the
      hashlib library. This way, the Sender is able to receive each ack with a checksum and compare it to the checksum it
      calculates for the ack (the checksum field it received in the ack is excluded in the calculation) and the receiver can
      do the same for the messages it receives. If the checksum is missing entirely in a packet due to corruption, we are
      able to check if the "checksum" key exists in the dictionary (representing a packet). Using md5 also limits our
      checksum to 16 bytes, which minimizes the packet size in comparison to using a hash algorithm that would produce a
      bigger checksum.

    - We significantly limited the amount of key values in packets to reduce the number of bytes in each packet and improve
      efficiency in regard to the time it takes for our program to run. Specifically, our packets from the Sender are
      formatted to have "data", "pk_num", and "checksum" and our acks are formatted to have "pk_num", and "checksum". Thus, 
      each of these values in the packets serve an important purpose, that cannot be achieved without them and are 
      essential to ensuring packets are ordered properly, not duplicates, and not corrupted.

    - We use helpers throughout our code to ensure readability. Implementing helpers to handle different functionalities of
      our code, such as for creating checksums, checking corruption, calculating RTT, logging and sending messages, 
      handling window sizing, etc. helped in the debugging process as well. When we encountered a bug in our code, and 
      determined potential sources of it, we were able to direct our eyes to certain, related helpers, rather than having 
      to sift through the entire code. Helpers also helped us understand how different parts of a reliable transport 
      protocol connect with each other.

    - To implement growing and shrink our window effectively, we implemented slow start. We did not restrict the receiver's
      buffer, so we were able to eliminate the window being capped by an advertised window. Implementing slow start allows
      our Sender to sending packets at an exponentially increasing rate unless we reach an arbitrary ss_thresh value or a
      packet drops. This improves efficiency the Sender is able to send increasingly more packets at once but also avoids 
      congestion. 
</pre><pre><b>Challenges Faced:</b>

During this project, we faced a few different challenges.
  
    - Our primary challenge occurred with our sliding window implementation. Initially, we were using a for loop on the 
      Sender side that iterated as many times as the window size (which was also hardcoded). For each iteration of the 
      for loop, a packet was sent. This required our Sender to send all the packets (with the limit being the window 
      size), then waiting for acks for all of these packets, and then sending another set of packets. This approach was 
      causing inefficient runtimes and resulted in timeouts for many of the tests. We modified this approach to be more 
      efficient by removing the loop. Instead, we control window primarily by the placement of where we set the waiting 
      flag to be True vs. False (on the Sender's side). Specifically, when an ack is received, the window grows and 
      another message can be sent to the Receiver. The Sender waits for an ack if the congestion window is less than or 
      equal to the amount of messages that have not been acked yet or if there are no more messages to send but there is 
      still acks to be received. The window shrinks if there is a timeout.

    - Another challenge we faced was in implementing corruption check. While we knew that we wanted to implement a 
      checksum to check for corruption, an issue we ran into was if a packet arrived corrupted such that it did not have 
      the checksum. In this scenario, we decided to implement a check on the Receiver's side for existence of the 
      checksum in the packet. If the Receiver detects corruption, it simply does not send an ack, so the Sender can 
      detect a timeout and resend the timed-out packet. On the Sender's side, we also check for corruption of an ack 
      before checking any other conditions (such as if it is a duplicate and if it is an expected ack), in case any of 
      the other fields in a packet such as "pk_num" were also corrupted.

    - A challenge we faced with level 5 tests was errors when trying to call dump() on a JSON packet to calculate the
     checksum. This was causing errors because we were passing improper JSONs, due to the packets received on both the 
     Sender and Receiver sides being corrupted. Thus, a try-except block was required around these, so we could 
     gracefully exit on an error.

    - Additionally, we faced a challenge with our RTT calculations (used to determine if a timeout has occurred). While 
      the formula for this was correct, we were calculating our new_sample value much later than we should have been. 
      We resolved this error by moving our new_sample calculation to the location where an ack is received.

    - Finally, a challenge we faced was timeouts due to limiting our window to grow only up to 4 packets. This was
      detected in level 8-2 tests, and we were able to resolve this by simply removing any idea of an advertised window 
      size from our code. 
</pre><pre><b>Testing:</b>

In this project, our primary method of testing code involved utilizing log/print statements and paying 
attention to timestamps for debugging purposes after executing the provided configuration test files. 
Using log/print messages allowed us to narrow down which part of our code was causing the issue. 
Comparing the given timestamps with the ones we created for the RTT implementation made it easier to 
tell if there were unexpected new_RTT values and also gave us the chance to check our program's efficiency. 
Because calculating the RTT seemed very simple and straightforward, paying attention to the self generated 
timestamps helped in figuring out if our tests were failing because of the sliding window functionality 
or calculating the new_sample value. We also used try-except blocks throughout our code to help us determine 
which lines of code were failing and exit gracefully. Also, we executed the code on both our local 
environments and on the Khoury VM to ensure performance across different setups/machines remains consistent.
</pre>

// Link to repo for better formatting: https://github.khoury.northeastern.edu/sindelar/3700-4/
