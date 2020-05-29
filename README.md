# VIVOTO - We'll make you a vivid memo
> android video & photo editor application with deep learning processing 

![lang](https://img.shields.io/github/languages/top/jsh-me/android-video-editor)![platform](https://img.shields.io/badge/platform-android-yellowgreen)![licence](https://img.shields.io/github/license/jsh-me/android-video-editor)



Simple Video Editor is android application that can help video, image inpainting work. Even if it's not for this purpose, it has enough function as a video editor. 

you can use video-time-line view, video trim based on ffmpeg engine, and select a specific frame to draw a line.



## Main UI Feature

There are four main feature in ui :-9

#### 1. Play a video

![1loadVideo](https://user-images.githubusercontent.com/39688690/82647357-0c3f9900-9c51-11ea-87d3-95fe55b67b05.gif)



#### 2. Trim a Video

![2cropVideo](https://user-images.githubusercontent.com/39688690/82647496-3f822800-9c51-11ea-8a79-4c5aee0e92de.gif)



#### 3. Select the specific frame and Draw a line

![3drawVideo](https://user-images.githubusercontent.com/39688690/82647547-532d8e80-9c51-11ea-82bc-c4810ecf30f9.gif)



#### 4. Save a result

![4saveVideo](https://user-images.githubusercontent.com/39688690/82647652-7d7f4c00-9c51-11ea-8724-8973a5b4db3c.gif)





## What is inpainting ?

Inpainting is a conservation process where damaged, deteriorating, or missing parts of an artwork are filled in to present a complete image. As a rule the **original file** and **corresponding binary mask file** are required to test inpainitng.

![img](https://miro.medium.com/max/978/1*s2bG37m-8g4sqioUC3T76w.png)



## How to generate a mask file in android

1. In the app logic, a user will draw lines on the specific frame (or photo). So I converted the frame(or photo) to bitmap and got the RGB pixel value of the bitmap.
2. If a RGB pixel of the bitmap exceeds a specific threshold, the pixel will mapping  to 0 value

3. Resize the bitmap with the same height and width as the original file.





### Original image and generated result mask image in android App

<img src="https://user-images.githubusercontent.com/39688690/82655327-f9cb5c80-9c5c-11ea-9215-6367014d5fdd.gif" width="80%">



#### Result: 

<img src="https://user-images.githubusercontent.com/39688690/82655031-8b869a00-9c5c-11ea-80bd-299288f10f8a.png" width="48%">  <img src="https://user-images.githubusercontent.com/39688690/82655208-ca1c5480-9c5c-11ea-937d-c9d317247330.png" width="48%">





## FAQ

feel free to contact me if you have and questions.

Email: ppm_it@naver.com



## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
