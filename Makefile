PROJECT = Hadoop
BUILDDIR = bin
SRC = Hadoop.java
OJS = $(patsubst %.java, $(BUILDDIR)/%.class, $(SRC))

.PHONY: run clean 

compile: $(BUILDDIR) $(OJS)

$(BUILDDIR)/%.class: %.java
	javac $^ -d $(BUILDDIR)

run: compile
	cd bin
	java Hadoop
	cd ..

clean:
	rm -rf $(BUILDDIR)

$(BUILDDIR):
	mkdir bin
	touch .dir
