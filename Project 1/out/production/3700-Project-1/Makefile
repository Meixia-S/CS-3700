CLASSPATH=.: 3700-Project-1/gson-2.10.1.jar

default: all

all: client.class Main.class

client.class: client.java
    javac -classpath $(CLASSPATH) client.java

Main.class: Main.java
    javac -classpath $(CLASSPATH) Main.java

clean:
    rm -f* .class
