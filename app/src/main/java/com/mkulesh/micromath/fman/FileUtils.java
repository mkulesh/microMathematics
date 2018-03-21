/*******************************************************************************
 * microMathematics Plus - Extended visual calculator
 * *****************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mkulesh.micromath.fman;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FileUtils
{
    public final static String ASSET_RESOURCE_PREFIX = "asset:/";

    public final static String C_AUDIO = "a", C_VIDEO = "v", C_TEXT = "t", C_ZIP = "z", C_OFFICE = "o", C_DROID = "d",
            C_BOOK = "b", C_IMAGE = "i", C_MARKUP = "m", C_APP = "x", C_PDF = "p", C_UNKNOWN = "u",
            C_MICROMATH = "mmt", C_SMATH_STUDIO = "SM";

    private final static String[][] mimes = { // should be sorted!
            { ".3gpp", "audio/3gpp", C_AUDIO },
            { ".7z", "application/x-7z-compressed", C_ZIP },
            { ".aif", "audio/x-aiff", C_AUDIO },
            { ".apk", "application/vnd.android.package-archive", C_DROID },
            { ".arj", "application/x-arj", C_ZIP },
            { ".au", "audio/basic", C_AUDIO },
            { ".avi", "video/x-msvideo", C_VIDEO },
            { ".b1", "application/x-b1", C_APP },
            { ".bmp", "image/bmp", C_IMAGE },
            { ".bz", "application/x-bzip2", C_ZIP },
            { ".bz2", "application/x-bzip2", C_ZIP },
            { ".cab", "application/x-compressed", C_ZIP },
            { ".chm", "application/vnd.ms-htmlhelp", C_OFFICE },
            { ".conf", "application/x-conf", C_APP },
            { ".csv", "text/csv", C_TEXT },
            { ".db", "application/x-sqlite3", C_APP },
            { ".dex", "application/octet-stream", C_DROID },
            { ".djvu", "image/vnd.djvu", C_IMAGE },
            { ".doc", "application/msword", C_OFFICE },
            { ".docx", "application/msword", C_OFFICE },
            { ".epub", "application/epub+zip", C_BOOK },
            { ".fb2", "application/fb2", C_BOOK },
            { ".flac", "audio/flac", C_AUDIO },
            { ".flv", "video/x-flv", C_VIDEO },
            { ".gif", "image/gif", C_IMAGE },
            { ".gtar", "application/x-gtar", C_ZIP },
            { ".gz", "application/x-gzip", C_ZIP },
            { ".htm", "text/html", C_MARKUP },
            { ".html", "text/html", C_MARKUP },
            { ".img", "application/x-compressed", C_ZIP },
            { ".jar", "application/java-archive", C_ZIP },
            { ".java", "text/java", C_TEXT },
            { ".jpeg", "image/jpeg", C_IMAGE },
            { ".jpg", "image/jpeg", C_IMAGE },
            { ".js", "text/javascript", C_TEXT },
            { ".lzh", "application/x-lzh", C_ZIP },
            { ".m3u", "audio/x-mpegurl", C_AUDIO },
            { ".md5", "application/x-md5", C_APP },
            { ".mid", "audio/midi", C_AUDIO },
            { ".midi", "audio/midi", C_AUDIO },
            { ".mkv", "video/x-matroska", C_VIDEO },
            { ".mmt", "application/micro-math", C_MICROMATH },
            { ".mobi", "application/x-mobipocket", C_BOOK },
            { ".mov", "video/quicktime", C_VIDEO },
            { ".mp2", "video/mpeg", C_VIDEO },
            { ".mp3", "audio/mp3", C_AUDIO },
            { ".mp4", "video/mp4", C_VIDEO },
            { ".mpeg", "video/mpeg", C_VIDEO },
            { ".mpg", "video/mpeg", C_VIDEO },
            { ".odex", "application/octet-stream", C_DROID },
            { ".ods", "application/vnd.oasis.opendocument.spreadsheet", C_OFFICE },
            { ".odt", "application/vnd.oasis.opendocument.text", C_OFFICE },
            { ".oga", "audio/ogg", C_AUDIO },
            { ".ogg", "audio/ogg", C_AUDIO }, // RFC 5334
            { ".ogv", "video/ogg", C_VIDEO }, // RFC 5334
            { ".opml", "text/xml", C_MARKUP },
            { ".pdf", "application/pdf", C_PDF },
            { ".php", "text/php", C_MARKUP },
            { ".pmd", "application/x-pmd", C_OFFICE }, //      PlanMaker Spreadsheet
            { ".png", "image/png", C_IMAGE },
            { ".ppt", "application/vnd.ms-powerpoint", C_OFFICE },
            { ".pptx", "application/vnd.ms-powerpoint", C_OFFICE },
            { ".prd", "application/x-prd", C_OFFICE }, //      SoftMaker Presentations Document
            { ".ra", "audio/x-pn-realaudio", C_AUDIO }, { ".ram", "audio/x-pn-realaudio", C_AUDIO },
            { ".rar", "application/x-rar-compressed", C_ZIP }, { ".rtf", "application/rtf", C_OFFICE },
            { ".sh", "application/x-sh", C_APP }, { ".so", "application/octet-stream", C_APP },
            { ".sm", "application/micro-math", C_SMATH_STUDIO },
            { ".sqlite", "application/x-sqlite3", C_APP }, { ".svg", "image/svg+xml", C_IMAGE },
            { ".swf", "application/x-shockwave-flash", C_VIDEO },
            { ".sxw", "application/vnd.sun.xml.writer", C_OFFICE },
            { ".tar", "application/x-tar", C_ZIP },
            { ".tcl", "application/x-tcl", C_APP },
            { ".tgz", "application/x-gzip", C_ZIP },
            { ".tif", "image/tiff", C_IMAGE },
            { ".tiff", "image/tiff", C_IMAGE },
            { ".tmd", "application/x-tmd", C_OFFICE }, //      TextMaker Document
            { ".txt", "text/plain", C_TEXT }, { ".vcf", "text/x-vcard", C_OFFICE }, { ".wav", "audio/wav", C_AUDIO },
            { ".wma", "audio/x-ms-wma", C_AUDIO }, { ".wmv", "video/x-ms-wmv", C_VIDEO },
            { ".xls", "application/vnd.ms-excel", C_OFFICE }, { ".xlsx", "application/vnd.ms-excel", C_OFFICE },
            { ".xml", "text/xml", C_MARKUP }, { ".xsl", "text/xml", C_MARKUP }, { ".zip", "application/zip", C_ZIP } };

    public final static String getMimeByExt(String ext, String defValue)
    {
        if (str(ext))
        {
            String[] descr = getTypeDescrByExt(ext);
            if (descr != null)
                return descr[1];
            // ask the system
            MimeTypeMap mime_map = MimeTypeMap.getSingleton();
            if (mime_map != null)
            {
                String mime = mime_map.getMimeTypeFromExtension(ext.substring(1));
                if (str(mime))
                    return mime;
            }
        }
        return defValue;
    }

    public final static String getCategoryByExt(String ext)
    {
        if (str(ext))
        {
            String[] descr = getTypeDescrByExt(ext);
            if (descr != null)
                return descr[2];
            // ask the system
            MimeTypeMap mime_map = MimeTypeMap.getSingleton();
            if (mime_map != null)
            {
                String mime = mime_map.getMimeTypeFromExtension(ext.substring(1));
                if (str(mime))
                {
                    String type = mime.substring(0, mime.indexOf('/'));
                    if (type.compareTo("text") == 0)
                        return C_TEXT;
                    if (type.compareTo("image") == 0)
                        return C_IMAGE;
                    if (type.compareTo("audio") == 0)
                        return C_AUDIO;
                    if (type.compareTo("video") == 0)
                        return C_VIDEO;
                    if (type.compareTo("application") == 0)
                        return C_APP;
                }
            }
        }
        return C_UNKNOWN;
    }

    public final static String[] getTypeDescrByExt(String ext)
    {
        ext = ext.toLowerCase(Locale.ENGLISH);
        int from = 0, to = mimes.length;
        for (int l = 0; l < mimes.length; l++)
        {
            int idx = (to - from) / 2 + from;
            String tmp = mimes[idx][0];
            if (tmp.compareTo(ext) == 0)
                return mimes[idx];
            int cp;
            for (cp = 1; ; cp++)
            {
                if (cp >= ext.length())
                {
                    to = idx;
                    break;
                }
                if (cp >= tmp.length())
                {
                    from = idx;
                    break;
                }
                char c0 = ext.charAt(cp);
                char ct = tmp.charAt(cp);
                if (c0 < ct)
                {
                    to = idx;
                    break;
                }
                if (c0 > ct)
                {
                    from = idx;
                    break;
                }
            }
        }
        return null;
    }

    public final static String getFileExt(String file_name)
    {
        if (file_name == null)
            return "";
        int dot = file_name.lastIndexOf(".");
        return dot >= 0 ? file_name.substring(dot) : "";
    }

    public final static String getSecondaryStorage()
    {
        try
        {
            Map<String, String> env = System.getenv();
            String sec_storage = env.get("SECONDARY_STORAGE");
            if (!FileUtils.str(sec_storage))
                return null;
            String[] ss = sec_storage.split(":");
            for (int i = 0; i < ss.length; i++)
                if (ss[i].toLowerCase(Locale.ENGLISH).indexOf("sd") > 0)
                    return ss[i];
            return "";
        }
        catch (Exception e)
        {
            // empty
        }
        return null;
    }

    static final char[] spaces = { '\u00A0', '\u00A0', '\u00A0', '\u00A0', '\u00A0', '\u00A0', '\u00A0', '\u00A0' };

    public final static String getHumanSize(long sz)
    {
        return getHumanSize(sz, true);
    }

    public final static String getHumanSize(long sz, boolean prepend_nbsp)
    {
        try
        {
            String s;
            if (sz > 1073741824)
                s = Math.round(sz * 10 / 1073741824.) / 10. + "G";
            else if (sz > 1048576)
                s = Math.round(sz * 10 / 1048576.) / 10. + "M";
            else if (sz > 1024)
                s = Math.round(sz * 10 / 1024.) / 10. + "K";
            else
                s = "" + sz + " ";
            if (prepend_nbsp)
                return new String(spaces, 0, 8 - s.length()) + s;
            else
                return s;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "" + sz + " ";
    }

    public final static String mbAddSl(String path)
    {
        if (!str(path))
            return "/";
        return path.charAt(path.length() - 1) == '/' ? path : path + "/";
    }

    public final static boolean str(String s)
    {
        return s != null && s.length() > 0;
    }

    public final static boolean equals(String s1, String s2)
    {
        if (s1 == null)
        {
            return s2 == null;
        }
        return s1.equals(s2);
    }

    public final static String escapeRest(String s)
    {
        if (!str(s))
            return s;
        return s.replaceAll("%", "%25").replaceAll("#", "%23").replaceAll(":", "%3A");
    }

    public final static String escapePath(String s)
    {
        if (!str(s))
            return s;
        return escapeRest(s).replaceAll("@", "%40");
    }

    public static Uri ensureScheme(final Uri uri)
    {
        if (uri.getScheme() == null)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("file").authority("").encodedPath(uri.getEncodedPath());
            return builder.build();
        }
        return uri;
    }

    public static InputStream getInputStream(final Context c, final Uri u)
    {
        return getInputStream(c, u, true);
    }

    public static InputStream getInputStream(final Context c, final Uri u, boolean showToastOnError)
    {
        try
        {
            InputStream is = null;
            if (isAssetUri(u))
            {
                final String asset = u.toString().replace(FileUtils.ASSET_RESOURCE_PREFIX, "");
                final AssetManager am = c.getAssets();
                is = am.open(asset);
            }
            else
            {
                final ContentResolver cr = c.getContentResolver();
                is = cr.openInputStream(u);
            }
            ViewUtils.Debug(c, "reading uri: " + u.toString());
            if (is != null)
            {
                return is;
            }
        }
        catch (Exception e)
        {
            final String error = String.format(c.getResources().getString(R.string.error_file_read),
                    u.getLastPathSegment());
            ViewUtils.Debug(c, error + ", " + e.getLocalizedMessage());
            if (showToastOnError)
            {
                Toast.makeText(c, error, Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    public static OutputStream getOutputStream(final Context c, final Uri u)
    {
        try
        {
            ContentResolver cr = c.getContentResolver();
            OutputStream os = cr.openOutputStream(u);
            ViewUtils.Debug(c, "writing uri: " + u.toString());
            if (os != null)
            {
                return os;
            }
        }
        catch (Exception e)
        {
            final String error = String.format(c.getResources().getString(R.string.error_file_write),
                    u.getLastPathSegment());
            ViewUtils.Debug(c, error + ", " + e.getLocalizedMessage());
            Toast.makeText(c, error, Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static void closeStream(Closeable stream)
    {
        try
        {
            stream.close();
        }
        catch (Exception e)
        {
            // nothing to do
        }
    }

    public static boolean isContentUri(Uri uri)
    {
        return uri != null && uri.getScheme() != null && uri.getScheme().equals("content");
    }

    public static boolean isAssetUri(Uri uri)
    {
        return uri != null && uri.getScheme() != null && uri.getScheme().equals("asset");
    }

    public static String getFileName(final Context c, final Uri uri)
    {
        String result = null;
        if (isContentUri(uri))
        {
            Cursor cursor = null;
            try
            {
                cursor = c.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            catch (Exception e)
            {
                ViewUtils.Debug(c, "cannot resolve file name: " + e.getLocalizedMessage());
            }
            finally
            {
                if (cursor != null)
                {
                    cursor.close();
                }
            }
        }
        else
        {
            final List<String> segments = uri.getPathSegments();
            if (segments != null && segments.size() > 0)
            {
                result = segments.get(segments.size() - 1);
            }
        }
        if (result == null)
        {
            result = uri.toString();
        }
        return result;
    }

    static public Uri getParentDirectory(final Uri uri)
    {
        final List<String> segments = uri.getPathSegments();
        if (segments == null || segments.size() <= 1)
        {
            return null;
        }

        final String[] newSegments = new String[segments.size() - 1];
        for (int i = 0; i < newSegments.length; i++)
        {
            newSegments[i] = segments.get(i);
        }
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(uri.getScheme());
        builder.encodedAuthority(uri.getAuthority());
        builder.encodedPath(TextUtils.join("/", newSegments));
        return builder.build();
    }

    static public Uri getParentUri(final Uri uri)
    {
        if (uri != null)
        {
            return (isContentUri(uri)) ? AdapterDocuments.getParent(uri) : getParentDirectory(uri);
        }
        return null;
    }

    public static Uri catUri(final Context context, final Uri folder, final String file)
    {
        if (file == null)
        {
            return null;
        }

        final Uri parsedFile = Uri.parse(file);
        if (folder == null || !parsedFile.isRelative())
        {
            return parsedFile;
        }

        final String[] nameParts = file.split("/", -1);
        Uri resUri = folder;
        for (String part : nameParts)
        {
            if (part == null || resUri == null)
            {
                break;
            }
            Uri newUri = null;
            if (AdapterBaseImpl.PLS.equals(part))
            {
                if (FileUtils.isContentUri(resUri))
                {
                    newUri = AdapterDocuments.getParent(resUri);
                }
                else
                {
                    newUri = FileUtils.getParentDirectory(resUri);
                }
            }
            else
            {
                if (FileUtils.isContentUri(resUri))
                {
                    newUri = AdapterDocuments.withAppendedPath(context, resUri, part);
                }
                else
                {
                    newUri = Uri.withAppendedPath(resUri, part);
                }
            }
            if (newUri != null)
            {
                resUri = newUri;
            }
        }
        return resUri;
    }

    public static String convertToRelativePath(final Uri absoluteUri, final Uri relativeToUri)
    {
        if (!absoluteUri.getScheme().equals(relativeToUri.getScheme()))
        {
            return null;
        }

        String absolutePath = null, relativeTo = null;
        if (isContentUri(absoluteUri))
        {
            absolutePath = AdapterDocuments.getPath(absoluteUri, true);
            relativeTo = AdapterDocuments.getPath(relativeToUri, true);
        }
        else
        {
            absolutePath = absoluteUri.getEncodedPath();
            relativeTo = relativeToUri.getEncodedPath();
        }
        if (absolutePath == null || relativeTo == null)
        {
            return null;
        }

        // Thanks to:
        // http://mrpmorris.blogspot.com/2007/05/convert-absolute-path-to-relative-path.html
        absolutePath = absolutePath.replaceAll("\\\\", "/");
        relativeTo = relativeTo.replaceAll("\\\\", "/");
        StringBuilder relativePath = null;

        if (!absolutePath.equals(relativeTo))
        {
            String[] absoluteDirectories = absolutePath.split("/");
            String[] relativeDirectories = relativeTo.split("/");

            //Get the shortest of the two paths
            int length = absoluteDirectories.length < relativeDirectories.length ? absoluteDirectories.length
                    : relativeDirectories.length;

            //Use to determine where in the loop we exited
            int lastCommonRoot = -1;
            int index;

            //Find common root
            for (index = 0; index < length; index++)
            {
                if (absoluteDirectories[index].equals(relativeDirectories[index]))
                {
                    lastCommonRoot = index;
                }
                else
                {
                    break;
                    //If we didn't find a common prefix then throw
                }
            }
            if (lastCommonRoot != -1)
            {
                //Build up the relative path
                relativePath = new StringBuilder();
                //Add on the ..
                for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++)
                {
                    if (absoluteDirectories[index].length() > 0)
                    {
                        relativePath.append("../");
                    }
                }
                for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++)
                {
                    relativePath.append(relativeDirectories[index] + "/");
                }
                relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
            }
        }
        return relativePath == null ? null : relativePath.toString();
    }

    public static long getAppTimeStamp(Context context)
    {
        try
        {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            String appFile = appInfo.sourceDir;
            return new File(appFile).lastModified();
        }
        catch (Exception e)
        {
            // nothing to do
        }
        return 0;
    }
}
