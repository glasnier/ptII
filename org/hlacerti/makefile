# Makefile for Ptolemy II - HLA/CERTI cooperation
#
# @Authors: Gilles Lasnier, based on a file by Christopher H. Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2013-2016 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		org/hlacerti

DIRS =      lib demo test

# Root of Ptolemy II directory
ROOT =      ../..

CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     hlacerti
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
        lib/lib.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

OTHER_FILES_TO_BE_JARED = \
	INSTALL_CERTI.txt \
	INSTALL_JCERTI.txt \
	INSTALL_VergilCERTI.txt \
	README_HLAPtides.txt \
	manual-ptii-hla.pdf \
	manual-ptii-hla-r72244.pdf \
	manual-ptii-hla-r73687.pdf \
	manual-ptii-hla-r74778.pdf

EXTRA_SRCS =    $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES = lib

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	test

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
