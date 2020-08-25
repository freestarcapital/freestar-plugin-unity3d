


"C:\Program Files\Unity.2019.2.2f1\Editor\Unity.exe" -gvh_disable       -batchmode       -importPackage c:\Users\kevin\Downloads\play-services-resolver-1.2.135.0.unitypackage       -projectPath c:\Dev\vdopia\samples\unity_samples\\ChocolateUnityPackageProject       -exportPackage Assets ChocolateMediationPlugin-1.0.6.unitypackage       -quit

First remove the old PlayServicesResolver folder: C:\Dev\vdopia\samples\unity_samples\ChocolateUnityPackageProject\Assets\PlayResolver

The Unity package will be dropped in C:\Dev\vdopia\samples\unity_samples\ChocolateUnityPackageProject\

C:\Dev\vdopia\samples\unity_samples\\ChocolateUnityPackageProject  - you build the .unitypackage from this project.  this is NOT the
project where you want to build the unity sample app.

choose any of the other projects to make a sample app from.

Create the unity plugin this way:
Remove the current PlayServicesResolver from C:\Dev\vdopia\samples\unity_samples\ChocolateUnityPackageProject\Assets\PlayResolver

"C:\Program Files\Unity.2019.2.2f1\Editor\Unity.exe" -gvh_disable       -batchmode       -importPackage c:\Users\kevin\Downloads\play-services-resolver-1.2.135.0.unitypackage       -projectPath c:\Dev\vdopia\samples\unity_samples\\ChocolateUnityPackageProject       -exportPackage Assets ChocolateMediationPlugin-1.0.6.unitypackage       -quit

First remove the old PlayServicesResolver folder: C:\Dev\vdopia\samples\unity_samples\ChocolateUnityPackageProject\Assets\PlayResolver

The Unity package will be dropped in C:\Dev\vdopia\samples\unity_samples\ChocolateUnityPackageProject\

Choose any other place to make the sample app.