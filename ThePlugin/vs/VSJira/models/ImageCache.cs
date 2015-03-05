using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Net;

namespace PaZu.models
{
    class ImageCache
    {
        private static readonly ImageCache INSTANCE = new ImageCache();

        public static ImageCache Instance { get { return INSTANCE; } }

        private readonly SortedDictionary<string, Image> cache = new SortedDictionary<string, Image>();

        public Image getImage(string url)
        {
            if (url == null)
            {
                return Properties.Resources.unknown;
            }
            lock (this)
            {
                if (cache.ContainsKey(url))
                {
                    return cache[url];
                }
                try
                {
                    Debug.WriteLine("ImageCache - Loading image for URL: " + url);
                    HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
                    request.Timeout = 5000;
                    request.ReadWriteTimeout = 20000;
                    HttpWebResponse response = (HttpWebResponse)request.GetResponse();
                    Image img = Image.FromStream(response.GetResponseStream());
                    cache[url] = img;
                    return img;
                }
                catch (Exception e)
                {
                    Debug.WriteLine(e.Message);
                    return Properties.Resources.unknown;
                }
            }
        }

        public void clear()
        {
            lock(this)
            {
                cache.Clear();
            }
        }
    }
}
