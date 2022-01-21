# KoikatsuOrNot
An attempt at using deep learning to differentiate between Koikatsu games vs. HS2 &amp; Daz

This was made using the [Kotlin Deeplearning API](https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlin-deeplearning-api/0.3.0), but only got to around 65% accuracy, and manually gathering the data was becoming a hassle, so I decided to let this one go.
Essentially what I was doing was going through game listings on a particular site, and then noting down which 'visual engine' they used. This program would then take the URL for that page and scrape it, downloading all of the images included in the first post of the thread.
These images were then curated, and I only picked the ones with a 16:9 resolution ratio (about 75% of the images I fetched, close to 1500), which were then resized and turned greyscale.
The accuracy might have improved if color was included, since there were obvious differences in color usage between the 'engines' but, this was my first project using deep learning, so I didn't feel like trying to figure that out at the same time as well.
I left the schema I used, but I can't imagine how useful that'd be if someone does end up forking this, so you'd have to figure out some data collection method thats viable.
Anyhow, I hope someone can possibly find a thing or two they might need from this :)
