# Makefile for a Ptolemy modeling framework for the Best Master Clock
# algorithm in IEEE1588.
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2011-2014 The Regents of the University of California.
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

ME =		ptolemy/demo/8275BMC

DIRS =

# Root of Ptolemy II directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	8275BMC
PTCLASSJAR =	$(PTPACKAGE).jar


EXAMPLE_MODELS = \
	1588ClockClass.xml \
	1588PortClass.xml \
	9ClockMesh.xml \
	Figure27-8Class.xml \
	ISPCS3clockRing.xml \
	ISPCSmultiClockRing.xml \
	ITSFchangePriority-v1.xml

OTHER_FILES_TO_BE_JARED = \
	$(EXAMPLE_MODELS) \
	BMCLibrary.xml

EXAMPLE_MODEL_HTML = $(EXAMPLE_MODELS:%.xml=%.htm)

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED) \

#	$(EXAMPLE_MODEL_HTML) \
#	index.htm \
#	toc.htm


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

OPTIONAL_FILES = 

# Files to remove with 'make clean'
KRUFT = \
	*.jar

all: suball $(EXTRA_SRCS)
install: subinstall jars

jnlps:
	cd $(ROOT); \
	set $(EXAMPLE_MODELS); \
	for x do \
		echo "Creating applet for $(ME)/$$x" ; \
		$(MAKE) JNLP_MODEL_DIRECTORY=$(ME) JNLP_MODEL=`basename $$x .xml` book_dist_update; \
	done

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
