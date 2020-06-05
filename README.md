# VIVOTO - We'll make you vivid memories
> android video & photo editor application with deep learning processing 

![lang](https://img.shields.io/github/languages/top/jsh-me/android-video-editor)![platform](https://img.shields.io/badge/platform-android-yellowgreen)![licence](https://img.shields.io/github/license/jsh-me/android-video-editor)



Simple Video Editor is android application that can help video, image inpainting work. Even if it's not for this purpose, it has enough function as a video editor. 

you can use video-time-line view, video trim based on ffmpeg engine, and select a specific frame to draw a line.



## Main UI Feature



|                          Play Video                          |                         Select Video                         |
| :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="https://user-images.githubusercontent.com/39688690/83832793-6ba7a980-a725-11ea-978d-aa49abb4bc1a.gif"/> | <img src="https://user-images.githubusercontent.com/39688690/83832938-ba554380-a725-11ea-802d-0514b239d8df.gif"/> |
|        **Select the specific frame and Draw a line**         |                       **Save Result**                        |
| ![gif (1)](https://user-images.githubusercontent.com/39688690/83498844-66651780-a4f7-11ea-8f11-6f648c8a3ec0.gif) | ![gif](https://user-images.githubusercontent.com/39688690/83498614-18501400-a4f7-11ea-8646-04fba2cca479.gif) |



## What is inpainting ?

Inpainting is a conservation process where damaged, deteriorating, or missing parts of an artwork are filled in to present a complete image. As a rule the **original file** and **corresponding binary mask file** are required to test inpainitng.

![img](https://miro.medium.com/max/978/1*s2bG37m-8g4sqioUC3T76w.png)



## How to generate a mask file in android

1. In the app logic, a user will draw lines on the specific frame (or photo). So I converted the frame(or photo) to bitmap and got the RGB pixel value of the bitmap.
2. If a RGB pixel of the bitmap exceeds a specific threshold, the pixel will mapping  to 0 value

3. Resize the bitmap with the same height and width as the original file.



## Original image and generated result mask image

<img src="https://user-images.githubusercontent.com/39688690/82655327-f9cb5c80-9c5c-11ea-9215-6367014d5fdd.gif" width="80%">



### Result

<img src="https://user-images.githubusercontent.com/39688690/82655031-8b869a00-9c5c-11ea-80bd-299288f10f8a.png" width="48%">  <img src="https://user-images.githubusercontent.com/39688690/82655208-ca1c5480-9c5c-11ea-937d-c9d317247330.png" width="48%">



## FAQ

feel free to contact me if you have and questions.

Email: ppm_it@naver.com



## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
