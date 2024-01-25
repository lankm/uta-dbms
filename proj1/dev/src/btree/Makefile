JAR=btreelib.jar

#this is the name of the given project folder
ASSIGNMENT=btree_project_spring_24

#change the ASSIGN path to the path where you have downloaded on your computer
ASSIGN=/home/s/sa/santraa/CSE5331

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
