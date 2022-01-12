package eu.essi_lab.request.executor.utils;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;

public class MimeTypeConstants {
    
    protected static Map<String,String> mapContentTypes = null;
    
        public static String getTypeExtension(String key)
    {
       String strMimeType = null;

       // get value for particular key
       strMimeType = mapContentTypes.get(key).toString();
       
//       if ((strMimeType == null) || (strMimeType.trim().length() == 0)){
//          strMimeType = s_strDefaultMimeType;
//       }
       
       return strMimeType;
    }
    
    
    static 
    {
       mapContentTypes = new HashMap<String, String>();
       mapContentTypes.put("application/postscript", "ai" );
       mapContentTypes.put( "audio/x-aiff", "aif");
       mapContentTypes.put("audio/x-aiff", "aifc");
       
       mapContentTypes.put("audio/x-aiff", "aiff");
       //mapContentTypes.put("text/plain", "asc");
       mapContentTypes.put("video/x.ms.asf", "asf");
       mapContentTypes.put("video/x.ms.asx", "asx");
       mapContentTypes.put("audio/basic", "au");
       mapContentTypes.put("video/x-msvideo", "avi");
       mapContentTypes.put("application/x-bcpio", "bcpio");
       mapContentTypes.put("application/octet-stream", "bin");
       mapContentTypes.put("application/x-cabinet", "cab");
       mapContentTypes.put("application/x-netcdf", "nc");
       mapContentTypes.put("application/java-vm" , "class");
       mapContentTypes.put("application/x-cpio", "cpio");
       mapContentTypes.put("application/mac-compactpro", "cpt");
       mapContentTypes.put("application/x-x509-ca-cert", "crt");
       mapContentTypes.put("application/x-csh", "csh");
       mapContentTypes.put("text/css", "css");
       mapContentTypes.put("text/comma-separated-values", "csv");
       mapContentTypes.put("application/x-director","dcr");
       mapContentTypes.put("application/x-director", "dir");
       mapContentTypes.put("application/x-msdownload", "dll");
       mapContentTypes.put("application/octet-stream", "dms");
       mapContentTypes.put("application/msword", "doc");
       mapContentTypes.put("application/xml-dtd", "dtd");
       mapContentTypes.put("application/x-dvi", "dvi");
       mapContentTypes.put("application/x-director", "dxr");
       mapContentTypes.put("application/postscript", "eps");
       mapContentTypes.put("text/x-setext","etx");
       mapContentTypes.put("application/octet-stream", "exe");
       mapContentTypes.put("application/andrew-inset", "ez");
       mapContentTypes.put("image/gif", "gif");
       //mapMimeTypes.put("application/x-gtar", "gtar");
       mapContentTypes.put("application/gzip", "gz");
       mapContentTypes.put("application/gzip", "gzip");
       mapContentTypes.put("application/x-hdf", "hdf");
       mapContentTypes.put("text/x-component", "htc");
       mapContentTypes.put("application/mac-binhex40", "hqx");
       mapContentTypes.put("text/html", "html");
       //mapMimeTypes.put("text/html", "htm");
       mapContentTypes.put("x-conference/x-cooltalk", "ice");
       mapContentTypes.put("image/ief", "ief");
       mapContentTypes.put("model/iges", "iges");
       mapContentTypes.put("model/iges", "igs");
       mapContentTypes.put("application/java-archive", "jar");
       //mapMimeTypes.put("text/plain", "java");
       mapContentTypes.put("application/x-java-jnlp-file", "jnlp");
       mapContentTypes.put("image/jpeg", "jpeg");
       mapContentTypes.put("image/jpe", "jpe");
       //mapMimeTypes.put("image/jpg", "jpg");
       mapContentTypes.put("application/x-javascript", "js");
       //mapMimeTypes.put("jsp", "text/plain");
       mapContentTypes.put("audio/midi", "kar");
       mapContentTypes.put("application/x-latex", "latex");
       //mapMimeTypes.put("lha", "application/octet-stream");
       //mapMimeTypes.put("lzh", "application/octet-stream");
       mapContentTypes.put("application/x-troff-man", "man");
       mapContentTypes.put("application/mathml+xml", "mathml");
       mapContentTypes.put("application/x-troff-me", "me");
       mapContentTypes.put("model/mesh", "mesh");
       mapContentTypes.put("audio/midi", "mid");
       //mapMimeTypes.put("midi", "audio/midi");
       mapContentTypes.put("application/vnd.mif", "mif");
       mapContentTypes.put("chemical/x-mdl-molfile", "mol");
       mapContentTypes.put("video/x-sgi-movie", "movie");
       mapContentTypes.put("video/quicktime", "mov");
       //mapMimeTypes.put("mp2", "audio/mpeg");
       mapContentTypes.put("audio/mpeg", "mp3");
       mapContentTypes.put("video/mpeg", "mpeg");
       //mapMimeTypes.put("mpe", "video/mpeg");
       //mapMimeTypes.put("mpga", "audio/mpeg");
       //mapMimeTypes.put("mpg", "video/mpeg");
       mapContentTypes.put("application/x-troff-ms", "ms");
       //mapMimeTypes.put("msh" ,"model/mesh");
       //mapMimeTypes.put("msi" ,"application/octet-stream");
       //mapMimeTypes.put("nc" ,"application/x-netcdf");
       mapContentTypes.put("application/oda", "oda");
       mapContentTypes.put("application/ogg", "ogg");
       mapContentTypes.put("image/x-portable-bitmap", "pbm" );
       mapContentTypes.put("chemical/x-pdb", "pdb");
       mapContentTypes.put("application/pdf", "pdf");
       mapContentTypes.put("image/x-portable-graymap", "pgm");
       mapContentTypes.put("application/x-chess-pgn", "pgn");
       mapContentTypes.put("image/png", "png");
       mapContentTypes.put("image/x-portable-anymap", "pnm");
       mapContentTypes.put("image/x-portable-pixmap", "ppm");
       mapContentTypes.put("application/vnd.ms-powerpoint", "ppt");
       mapContentTypes.put("application/postscript", "ps");
       //mapMimeTypes.put("qt" ,"video/quicktime");
       mapContentTypes.put("audio/x-pn-realaudio", "ra");
       mapContentTypes.put("audio/x-realaudio", "ra" );
       mapContentTypes.put("audio/x-pn-realaudio", "ram");
       mapContentTypes.put("image/x-cmu-raster", "ras");
       mapContentTypes.put("application/rdf+xml", "rdf");
       mapContentTypes.put("image/x-rgb", "rgb");
       //mapMimeTypes.put("rm" ,"audio/x-pn-realaudio");
       mapContentTypes.put("application/x-troff", "roff");
       mapContentTypes.put("application/x-rpm", "rpm");
       //mapMimeTypes.put("rpm" ,"audio/x-pn-realaudio");
       mapContentTypes.put("application/rtf", "rtf");
       mapContentTypes.put("text/richtext", "rtx");
       mapContentTypes.put("application/java-serialized-object", "ser");
       mapContentTypes.put("text/sgml", "sgml");
       mapContentTypes.put("text/sgm", "sgm");
       mapContentTypes.put("application/x-sh", "sh");
       mapContentTypes.put("application/x-shar", "shar");
       //mapMimeTypes.put("silo" ,"model/mesh");
       mapContentTypes.put("application/x-stuffit", "sit");
       mapContentTypes.put("application/x-koan", "skd");
       //mapMimeTypes.put("skm" ,"application/x-koan");
       //mapMimeTypes.put("skp" ,"application/x-koan");
       //mapMimeTypes.put("skt" ,"application/x-koan");
       mapContentTypes.put("application/smi", "smi");
       mapContentTypes.put("application/smil", "smil");
       mapContentTypes.put("audio/basic", "snd");
       mapContentTypes.put("application/x-futuresplash", "spl");
       mapContentTypes.put("application/x-wais-source", "src");
       mapContentTypes.put("application/x-sv4cpio", "sv4cpio");
       mapContentTypes.put("application/x-sv4crc", "sv4crc");
       mapContentTypes.put("image/svg+xml", "svg");
       mapContentTypes.put("application/x-shockwave-flash", "swf");
       //mapMimeTypes.put("t" ,"application/x-troff");
       mapContentTypes.put("application/x-tar", "tar");
       mapContentTypes.put("application/x-gtar", "tar.gz");
       mapContentTypes.put("application/x-tcl", "tcl");
       mapContentTypes.put("application/x-tex", "tex");
       //mapMimeTypes.put("application/x-texinfo", "texi");
       mapContentTypes.put("application/x-texinfo", "texinfo");
       //mapMimeTypes.put("tgz" ,"application/x-gtar");
       mapContentTypes.put("image/tiff", "tiff");
       //mapMimeTypes.put("tif" ,"image/tiff");
       //mapMimeTypes.put("application/x-troff", "tr");
       mapContentTypes.put("text/tab-separated-values", "tsv");
       mapContentTypes.put("text/plain", "txt");
       mapContentTypes.put("application/x-ustar", "ustar");
       mapContentTypes.put("application/x-cdlink", "vcd");
       mapContentTypes.put("model/vrml", "vrml");
       mapContentTypes.put("application/voicexml+xml", "vxml");
       mapContentTypes.put("audio/x-wav", "wav");
       mapContentTypes.put("image/vnd.wap.wbmp", "wbmp");
       mapContentTypes.put("application/vnd.wap.wmlc", "wmlc");
       mapContentTypes.put("application/vnd.wap.wmlscriptc", "wmlsc" );
       mapContentTypes.put("text/vnd.wap.wmlscript", "wmls");
       mapContentTypes.put("text/vnd.wap.wml", "wml");
       mapContentTypes.put("model/vrml", "wrl");
       mapContentTypes.put("application/vnd.wap.wtls-ca-certificate", "wtls-ca-certificate");
       mapContentTypes.put("image/x-xbitmap", "xbm");
       //mapMimeTypes.put("xht" ,"application/xhtml+xml");
       mapContentTypes.put("application/xhtml+xml", "xhtml");
       mapContentTypes.put("application/vnd.ms-excel", "xls");
       mapContentTypes.put("application/xml", "xml");
       mapContentTypes.put("image/x-xpixmap", "xpm");
       //mapMimeTypes.put("xpm", "image/x-xpixmap");
       mapContentTypes.put("application/xml", "xsl");
       mapContentTypes.put("application/xslt+xml", "xslt");
       mapContentTypes.put("application/vnd.mozilla.xul+xml", "xul");
       mapContentTypes.put("image/x-xwindowdump", "xwd");
       mapContentTypes.put("chemical/x-xyz", "xyz");
       mapContentTypes.put("application/compress", "z");
       mapContentTypes.put("application/zip", "zip");
       
       
       mapContentTypes.put("application/vnd.google-earth.kml+xml", "kml");
       mapContentTypes.put("application/vnd.google-earth.kmz", "kmz");
       
       //word
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
       mapContentTypes.put("application/vnd.ms-word.document.macroEnabled.12", "docm");
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
       mapContentTypes.put("application/vnd.ms-excel.sheet.macroEnabled.12", "xlsm");
       mapContentTypes.put("application/vnd.ms-excel.template.macroEnabled.12", "xltm");
       mapContentTypes.put("application/vnd.ms-excel.addin.macroEnabled.12", "xlam");
       mapContentTypes.put("application/vnd.ms-excel.sheet.binary.macroEnabled.12", "xlsb");
       
       
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
       mapContentTypes.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
       mapContentTypes.put("application/vnd.ms-powerpoint.addin.macroEnabled.12", "ppam");
       mapContentTypes.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12", "pptm");
       mapContentTypes.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", "ppsm");
       
       // Open Office
       mapContentTypes.put("application/vnd.oasis.opendocument.text", "odt");
       mapContentTypes.put("application/vnd.oasis.opendocument.text-template", "ott");
       mapContentTypes.put("application/vnd.oasis.opendocument.text-web", "oth");
       mapContentTypes.put("application/vnd.oasis.opendocument.text-master", "odm");
       mapContentTypes.put("application/vnd.oasis.opendocument.graphics", "odg");
       mapContentTypes.put("application/vnd.oasis.opendocument.graphics-template", "otg");
       mapContentTypes.put("application/vnd.oasis.opendocument.presentation", "odp");
       mapContentTypes.put("application/vnd.oasis.opendocument.presentation-template", "otp");
       mapContentTypes.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
       mapContentTypes.put("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
       mapContentTypes.put("application/vnd.oasis.opendocument.chart", "odc");
       mapContentTypes.put("application/vnd.oasis.opendocument.formula", "odf");
       mapContentTypes.put("application/vnd.oasis.opendocument.database", "odb");
       mapContentTypes.put("application/vnd.oasis.opendocument.image", "odi");
       mapContentTypes.put("application/vnd.openofficeorg.extension", "oxt");
      
       
    }
    
    public static void main(String[] args) {
	String res = MimeTypeConstants.getTypeExtension("application/pdf");
	System.out.println(res);
    }
    
}

