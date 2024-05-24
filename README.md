Connection openCV:
gradle-wrapper
distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip

build.gradle.kts
implementation ("com.quickbirdstudios:opencv-contrib:3.4.15")

libs.versions
agp = "8.4.0"

if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!")
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!")

https://github.com/ramlaxmangroup/OpenCV-QuickbirdStudios/tree/master
https://github.com/opencv/opencv/tree/master/data/haarcascades x_x
