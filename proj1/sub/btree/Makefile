JAR=btreelib.jar

#this is the name of the given project folder
ASSIGNMENT=proj1/dev

#change the ASSIGN path to the path where you have downloaded on your computer
ASSIGN=/mnt/c/Users/lankm/Documents/online-repositories/uta-dbms

# Do not change the following paths if you are using omega.uta.edu (Spring 2024)
LIBPATH = $(ASSIGN)/$(ASSIGNMENT)/lib/$(JAR)
CLASSPATH = $(LIBPATH):$(ASSIGN)/$(ASSIGNMENT)/src
JAVAC = javac -classpath $(CLASSPATH)
JAVA  = java  -classpath $(CLASSPATH)


PROGS = together

all: $(PROGS)

together:*.java
	$(JAVAC) *.java

clean:
	\rm -f *.class *~ \#* core
