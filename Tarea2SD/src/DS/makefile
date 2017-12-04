JFLAGS = -g 
#-Xlint
JC = javac
JVM = java
PUERTO = 12345
POLITICA = -Djava.security.policy
ARCHIVO = permisos


.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
	Inter.java \
	Process.java \
	Server.java \

MAIN = Process

#default: classes
#	$(JVM) $(MAIN)

classes: $(CLASSES:.java=.class)

process: classes
ifeq ($(MAIN),Server)
	$(JVM) $(POLITICA)=$(ARCHIVO) $(MAIN) $(PUERTO)
else
	$(JVM) $(POLITICA)=$(ARCHIVO) $(MAIN) $(ID) $(N) $(DELAY) $(BEARER)
endif	

clean:
	$(RM) *.class

