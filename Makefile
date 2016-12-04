JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	project2/Node.java \
	project2/Client.java \
	project2/Server.java \
	project2/distance_vector_routing.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class