<h1 align="center">
    Simple Android Editor</h1>

<p align="center">
     <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
    <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a><a href=""><img alt="API" src="https://img.shields.io/github/languages/top/jsh-me/android-video-editor"/></a>
<a href=""><img alt="Android" src="https://img.shields.io/badge/platform-android-yellowgreen"/></a></p>

<p align="center">Android video & photo editor application using deep learning</br>(Super Resolution, Video & Image Inpainting)</br></br>
Simple Video Editor is android application that can help video, image inpainting work.</br> Even if it's not for this purpose, it has enough function as a video editor. </br></br>you can use video-time-line view, video trim based on ffmpeg engine,</br> and select a specific frame to draw a line.</p></br>

## Main UI Feature

|                          Play Video                          |                     Select to video trim                     |
| :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="https://user-images.githubusercontent.com/39688690/83832793-6ba7a980-a725-11ea-978d-aa49abb4bc1a.gif"/> | <img src="https://user-images.githubusercontent.com/39688690/83832938-ba554380-a725-11ea-802d-0514b239d8df.gif"/> |
|        **Select the specific frame and draw a mask**         |                      **Sebd to server**                      |
| ![gif (1)](https://user-images.githubusercontent.com/39688690/83498844-66651780-a4f7-11ea-8f11-6f648c8a3ec0.gif) | ![gif](https://user-images.githubusercontent.com/39688690/83498614-18501400-a4f7-11ea-8646-04fba2cca479.gif) |



## What is inpainting ?

Inpainting is a conservation process where damaged, deteriorating, or missing parts of an artwork are filled in to present a complete image. As a rule the **original file** and **corresponding binary mask file** are required to test inpainitng.

![img](https://miro.medium.com/max/978/1*s2bG37m-8g4sqioUC3T76w.png)



## How to generate a mask file in android

1. In the app logic, a user will draw lines on the specific frame (or photo). So I converted the frame(or photo) to bitmap and got the RGB pixel value of the bitmap.
2. If a RGB pixel of the bitmap exceeds a specific threshold, the pixel will mapping  to 0 value

3. Resize the bitmap with the same height and width as the original file.

<img src="https://user-images.githubusercontent.com/39688690/82655327-f9cb5c80-9c5c-11ea-9215-6367014d5fdd.gif" width="50%">



## Result

<img src="https://user-images.githubusercontent.com/39688690/82655031-8b869a00-9c5c-11ea-80bd-299288f10f8a.png" width="48%">  <img src="https://user-images.githubusercontent.com/39688690/82655208-ca1c5480-9c5c-11ea-937d-c9d317247330.png" width="48%">



## ✔ Check

@jsh-me 💬

This project is **WIP**

Feel free to contact me if you have and questions.

**Always welcome to FEEDBACK ;^) waiting for your contribution.**

Email: ppm_it@naver.com



## License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
