# 3700-Project-1
<pre>
High-level Approach:

I wanted to seperate the guessing of the word to be seperate from playing through the game. That way
seemed to make sense as both tasks are quite different. I also somewhat imaged the client class to
be the player and the Main class was the middle man that connected the player with the game (server)
and played it through. 
</pre> <pre>
Guessing Strategy:

I would summarize my logic as a seires of loops and checks. For each guess a for loop would travers 
the entire list of possible words. 

Each word would first check to see if we have already guessed the word. If so break from the loops, 
else start traversing through the word letter by letter.

Going letter by letter we check the current word with the previously guessed words using the marks.
Each letter would then be added to a HashMap that logged the letter with its corresponding mark
or index number. (ex: mark 2 => (w, 3), mark 1 = (q, 5), mark 0 => (w, 6))

After that loop (back in the first overarching one), a helper function is then called to actually
check if the word is correct enough to make a guess with it. This is done by going through the 
Hashmap and make sure all 6 are not present in the word, all 5 appear at least once in the word,
and all 0-4 are present in the index saved. If this check failed we added it to the already 
guessed list and moved on to the next word in the list. If it passes we send it to the server in 
the JSON format.
</pre><pre>
Testing: 

Due to the simplicity of the program, I used print statements to test/check my work. 
</pre><pre>
Challenges:

The logic of the guessing and gameplay was fairly easy: what I did struggle with was the makefile. 
I have never had to write one myself up until this point so this was totally new to me and took me
some time to get it right. There were no other note-worthy issues I had to deal with. My code 
needed a bit of debugging but it did not take long to fix the logic.
</pre>
