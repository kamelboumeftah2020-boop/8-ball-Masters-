package app.nur.quran;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.getcapacitor.Bridge;
import com.getcapacitor.BridgeWebViewClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * يقدّم الصوتيات المحمّلة (files/audio) للواجهة عبر https://localhost/_nur_audio_/
 * مع دعم صحيح لطلبات Range، ليعمل التقديم والاستكمال في المواعظ والسور المحمّلة.
 */
public class NurWebViewClient extends BridgeWebViewClient {

    private static final String PREFIX = "/_nur_audio_/";
    private final File audioDir;

    public NurWebViewClient(Bridge bridge) {
        super(bridge);
        audioDir = new File(bridge.getContext().getFilesDir(), "audio");
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String path = request.getUrl().getPath();
        if (path == null || !path.startsWith(PREFIX)) return super.shouldInterceptRequest(view, request);
        String name = path.substring(PREFIX.length());
        if (name.isEmpty() || name.contains("/") || name.contains("..")) return notFound();
        File file = new File(audioDir, name);
        if (!file.isFile()) return notFound();
        try {
            long total = file.length();
            long start = 0, end = total - 1;
            String range = request.getRequestHeaders().get("Range");
            if (range == null) range = request.getRequestHeaders().get("range");
            boolean partial = false;
            if (range != null && range.startsWith("bytes=")) {
                String[] p = range.substring(6).split("-", 2);
                if (!p[0].isEmpty()) start = Long.parseLong(p[0].trim());
                if (p.length > 1 && !p[1].trim().isEmpty()) end = Math.min(Long.parseLong(p[1].trim()), total - 1);
                if (start > end || start >= total) return notSatisfiable(total);
                partial = true;
            }
            long length = end - start + 1;
            InputStream in = new FileInputStream(file);
            long skipped = 0;
            while (skipped < start) {
                long s = in.skip(start - skipped);
                if (s <= 0) break;
                skipped += s;
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept-Ranges", "bytes");
            headers.put("Content-Length", String.valueOf(length));
            headers.put("Cache-Control", "no-cache");
            if (partial) headers.put("Content-Range", "bytes " + start + "-" + end + "/" + total);
            return new WebResourceResponse("audio/mpeg", null, partial ? 206 : 200, partial ? "Partial Content" : "OK", headers, new Limited(in, length));
        } catch (IOException | NumberFormatException e) {
            return notFound();
        }
    }

    private static WebResourceResponse notFound() {
        return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", new HashMap<>(), null);
    }

    private static WebResourceResponse notSatisfiable(long total) {
        Map<String, String> h = new HashMap<>();
        h.put("Content-Range", "bytes */" + total);
        return new WebResourceResponse("text/plain", "utf-8", 416, "Range Not Satisfiable", h, null);
    }

    /** يقرأ عددًا محددًا من البايتات فقط. */
    private static final class Limited extends FilterInputStream {
        private long left;

        Limited(InputStream in, long left) {
            super(in);
            this.left = left;
        }

        @Override
        public int read() throws IOException {
            if (left <= 0) return -1;
            int b = super.read();
            if (b >= 0) left--;
            return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (left <= 0) return -1;
            int n = super.read(buf, off, (int) Math.min(len, left));
            if (n > 0) left -= n;
            return n;
        }
    }
}
