/* A GDP Writer Test application.

   Copyright (c) 2014 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

*/

package org.terraswarm.gdp;


import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.ptolemy.fmi.NativeSizeT;

import org.terraswarm.gdp.EP_STAT;
import org.terraswarm.gdp.Gdp10Library;
import org.terraswarm.gdp.Gdp10Library.gdp_gcl_t;
import org.terraswarm.gdp.ep_stat_to_string;

import ptolemy.util.StringUtilities;

/** Useful utilities for working with the Global Data Plane.

    <p>This methods in this class are in a separate file so that we can
    regenerate the Java interface to the GDP and not overwrite this
    file.</p>

    @author Christopher Brooks, based on the gdp/apps/writer-test.c by Eric Allman.
    @version $Id: AccessorOne.java 69931 2014-08-29 23:36:38Z eal $
    @since Ptolemy II 10.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class GdpUtilities {

    /** Create a new status for GDP
     * Based on:
     * #define GDP_STAT_NEW(sev, det)		EP_STAT_NEW(EP_STAT_SEV_ ## sev, EP_REGISTRY_UCB, GDP_MODULE, det)
     * @param sev The severity
     * @param det FIXME
     * @return the status.
     */
    public static EP_STAT GDP_STAT_NEW(int sev, int det) {
	return EP_STAT_NEW(/* EP_STAT_SEV_ ## */ sev, EP_REGISTRY_UCB, GDP_MODULE, det);
    }

    /** Return true if the status code is ok.
     *  Based on ep_stat.h, Copyright Eric Allman, See ep_license.htm
     *  @param estat The status code.
     *  @return true if the code is less than EP_STAT_SEV_WARN
     */
    public static boolean EP_STAT_ISOK(EP_STAT estat) {
        long code = estat.code.longValue();
        //(((c).code >> _EP_STAT_SEVSHIFT) & ((1UL << _EP_STAT_SEVBITS) - 1))
        long EP_STAT_SEVERITY = (code >> Gdp10Library._EP_STAT_SEVSHIFT) & ((1l << Gdp10Library._EP_STAT_SEVBITS) - 1);

        //_debug("EP_STAT_ISOK(): code: " + code + ", EP_STAT_SEVERITY: " + EP_STAT_SEVERITY 
        //        + ", EP_STAT_SEV_WARN: " + EP_STAT_SEV_WARN 
        //        + "EP_STAT_SEVERITY < EP_STAT_SEV_WARN: " + (EP_STAT_SEVERITY < EP_STAT_SEV_WARN));
        return EP_STAT_SEVERITY < Gdp10Library.EP_STAT_SEV_WARN;
    }
    
    /** Create a new status code.
     *  Based on ep_stat.h, Copyright Eric Allman, See ep_license.htm
     *  @param s The severity
     *  @param r the registry
     *  @param m the module
     *  @param d the detail.
     *  @return the status code.
     */
    public static EP_STAT EP_STAT_INTERNAL(int sev, int mod, int code) {
        //#define _EP_STAT_INTERNAL(sev, mod, code)                     \
	//	EP_STAT_NEW(EP_STAT_SEV_ ## sev, EP_REGISTRY_EPLIB, mod, code)
        return EP_STAT_NEW(sev, EP_REGISTRY_EPLIB, mod, code);
    }
    /** Create a new status code.
     *  Based on ep_stat.h, Copyright Eric Allman, See ep_license.htm
     *  @param s The severity
     *  @param r the registry
     *  @param m the module
     *  @param d the detail.
     *  @return the status code.
     */
    public static EP_STAT EP_STAT_NEW(int s, int r, int m, int d) {
        // We use the same parameter names as are in the original C
        // code for ease of maintenance.
        long code = ((((s) & ((1l << Gdp10Library._EP_STAT_SEVBITS) - 1)) << Gdp10Library._EP_STAT_SEVSHIFT) | 
                (((r) & ((1l << Gdp10Library._EP_STAT_REGBITS) - 1)) << Gdp10Library._EP_STAT_REGSHIFT) | 
                (((m) & ((1l << Gdp10Library._EP_STAT_MODBITS) - 1)) << Gdp10Library._EP_STAT_MODSHIFT) |
                (((d) & ((1l << Gdp10Library._EP_STAT_DETBITS) - 1))));
        return new EP_STAT( new NativeLong(code));
    }                    

    /** Print the datum to standard out.  This is a port of
     *  gdp_datum_print from gdp/gdp_datum.c by Eric Allman.
     *  @param datum The datum to be printed.
     */   
    public static void gdp_datum_print(/*gdp_datum*/PointerByReference datum) {

        Pointer d = null;
        NativeSizeT length = new NativeSizeT();
        length.setValue(-1);
        if (datum == null) {
            System.out.print("null datum");
        }
        System.out.print("GDP record " + 
                Gdp10Library.INSTANCE.gdp_datum_getrecno(datum) + ", ");
        PointerByReference dbuf = Gdp10Library.INSTANCE.gdp_datum_getbuf(datum);
        if (dbuf == null) {
            System.out.print("no data");
        } else {
            // In gdp_api.c, gdp_datum_print() calls:
            // l = gdp_buf_getlength(datum->dbuf);
            length = Gdp10Library.INSTANCE.gdp_buf_getlength(dbuf);
            System.out.print("len " + length);

            // In gdp_api.c, this method calls:
            // d = gdp_buf_getptr(datum->dbuf, l);
            // gdp_buf.h has:
            // #define gdp_buf_getptr(b, z)	evbuffer_pullup(b, z)
            // So, we would need to call evbuffer_pullup() here.

            // A different idea would be to have a gdp_buf.c method
            // that calls evbuffer_pullup so that we don't need to run
            // JNA on that class.

            //d = Event2Library.INSTANCE.evbuffer_pullup(new evbuffer(datum.dbuf.getValue()), new NativeLong(length.longValue()));
            d = Gdp10Library.INSTANCE.gdp_buf_getptr(dbuf, new NativeSizeT(length.longValue()));
        }
        //Gdp10Library.INSTANCE.gdp_buf_getts(dbuf, new NativeSizeT(length.longValue()));
        EP_TIME_SPEC ts = new EP_TIME_SPEC();
        Gdp10Library.INSTANCE.gdp_datum_getts(datum, ts);

        if (ts.tv_sec != Long.MIN_VALUE) {
            // Was: ep_time_print(&datum->ts, fp, true);

            //final int TBUF_SIZE = 128;
            //ByteBuffer tbuf = ByteBuffer.allocate(TBUF_SIZE);
            //Gdp10Library.INSTANCE.ep_time_format(ts, tbuf, new NativeSizeT(TBUF_SIZE), (byte)1 /*human*/);
            //System.out.println("ts: " + ts + "tbuf: " + tbuf);

            System.out.println("tv_sec: " + ts.tv_sec + ", tv_nsec: " + ts.tv_nsec + " +/- " + ts.tv_accuracy);
        } else {
            System.out.print(", no timestamp ");
        }

        if (length.longValue() > 0) {
            // gdp_api.c has
            //fprintf(fp, "\n	 %s%.*s%s", EpChar->lquote, l, d, EpChar->rquote);
            System.out.print("\n  \"" + d.getString(0) + "\"");

        }
        System.out.println("");
    }

    /** Vendor registry for EP, see ep_registry.h */
    public static final int  EP_REGISTRY_EPLIB = 0x100;

    /** Vendor registry for UCB, see ep_registry.h */
    public static final int  EP_REGISTRY_UCB = 0x103;

    /** End of file. */
    public static final EP_STAT EP_STAT_END_OF_FILE;

    /** Corresponds to errno codes in C. */
    public static final int EP_STAT_MOD_ERRNO  = 0x0FE;

    /**  Basic multi-use errors. */
    public static final int EP_STAT_MOD_GENERIC = 0;

    /** The ok status code. */
    public static final EP_STAT EP_STAT_OK = EP_STAT_NEW(Gdp10Library.EP_STAT_SEV_OK, 0, 0, 0);

    /** Normal error. */
    public static final int EP_STAT_SEV_ERROR = 5;

    /** Warning or temp error, may work later. */
    public static final int EP_STAT_SEV_WARN = 4;

    /** Returned data. */
    public static final int GDP_EVENT_DATA = 1;

    /** End of subscription. */
    public static final int GDP_EVENT_EOS = 2;

    /** Not Found (404). From gdp_stat.h */
    public static final int GDP_COAP_NOTFOUND =	404;

    /** From gdp_stat.h */
    public static final int GDP_MODULE = 1;

    /** FIXME: What is a NAK NOTFOUND? */
    public static final EP_STAT GDP_STAT_NAK_NOTFOUND;

    static {
        // Define these in a static block so that we can alphabetize
        // the declarations and avoid a forward reference.
        EP_STAT_END_OF_FILE = EP_STAT_INTERNAL(EP_STAT_SEV_WARN, EP_STAT_MOD_GENERIC, 3);
        GDP_STAT_NAK_NOTFOUND = GDP_STAT_NEW(EP_STAT_SEV_ERROR, GDP_COAP_NOTFOUND);
    }
}

