#! /usr/bin/awk
# Awk script to generate Sun Java HTML compatible files
#
# Author:  Christopher Hylands
# Version: $Id$
#
# Copyright (c) 2001-2003 The Regents of the University of California.
# 	All Rights Reserved.
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

# This awk script is used to generate vergil compatible html files by
# reading in a html file and printing out the header up to and including
# <body>, and then only printing sections of the input that start
# with <!--vergilDocs--> and end with <!--/vergilDocs-->
# 
# To run this script, do
# awk -f javahtml.awk foo.htm > fooVergil.htm

BEGIN { print "<!-- DO NOT EDIT this file, it is created by running make."
	print "     The file to edit is $PTII/doc/ptIIx.x.release.htm"
	print "     where x.x is the release number, then run 'make update'"
        print "     to update the derived files and check them in to cvs. -->"
}
$0 ~ /<body.*>/ {
    sawBody = 1;
    # Remove any background info from the body tag.
    print "<body>";
}
{
    if (sawBody!=1) {
	print $0;
    } else {
	if ($0 ~/vergilDocs/) {
	    sawVergilDocs=1;
	    if ($0 ~/\/vergilDocs/) {
		sawVergilDocs=0;
	    }
	}
	if (sawVergilDocs==1) {
	    print $0;
	}
    }
}
END {
    print "</body>";
    print "</html>";
}
