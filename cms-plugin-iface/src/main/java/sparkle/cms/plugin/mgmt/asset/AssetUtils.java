package sparkle.cms.plugin.mgmt.asset;

import sparkle.cms.domain.AssetType;

import javax.activation.MimetypesFileTypeMap;

/**
 * AssetUtils
 * Created by bazzoni on 23/05/2015.
 */
public class AssetUtils {

    /**
     * Determine asset type according to mime content type
     *
     * @param contentType mime content type
     * @return @AssetType
     */
    public static AssetType findAssetTypeByContentType(String contentType) {
        AssetType assetType = AssetType.BINARY;
        if (contentType.equals("application/pdf")) {
            assetType = AssetType.PDF;
        } else if (contentType.startsWith("image")) {
            assetType = AssetType.IMAGE;
        } else if (contentType.startsWith("audio")) {
            assetType = AssetType.AUDIO;
        } else if (contentType.startsWith("video") || contentType.equals("application/mp4")) {
            assetType = AssetType.VIDEO;
        } else if (contentType.equals("application/msword") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/vnd.oasis.opendocument.text")) {
            assetType = AssetType.DOCUMENT;
        } else if (contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.oasis.opendocument.spreadsheet")) {
            assetType = AssetType.SPREADSHEET;
        } else if (contentType.equals("application/vnd.ms-powerpoint") || contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                contentType.equals("application/vnd.oasis.opendocument.presentation")) {
            assetType = AssetType.PRESENTATION;
        } else if (contentType.equals("application/zip") || contentType.equals("application/gzip") || contentType.startsWith("application/x")) {
            assetType = AssetType.ZIP;
        } else if (contentType.equals("text/plain")) {
            assetType = AssetType.TEXT;
        }
        return assetType;
    }

    /**
     * Find mime content type acc0rding to file name
     *
     * @param name file name
     * @return mime content type
     */
    public static String findContentTypeByFileName(String name) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        // only by file name
        return mimeTypesMap.getContentType(name);
    }

    /**
     * Find asset type according to file name
     *
     * @param name file name
     * @return @AssetType
     */
    public static AssetType findAssetTypeByFileName(String name) {
        return findAssetTypeByContentType(findContentTypeByFileName(name));
    }
}
