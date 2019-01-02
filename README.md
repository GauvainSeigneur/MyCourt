# My Court
A mobile application which uses Dribbble API V2. It focuses on Prospect, Players and pros. 
It is based on MVVM architecture & Android Architecture components. 

<img src="art/home_page.png" width="20%"></img>
<img src="art/details.png" width="20%"></img>
<img src="art/draft_page.png" width="20%"></img>
<img src="art/draft2.png" width="20%"></img>

## Features
* Fetch your shots in a endless recyclerView (paging library)
* See details of each shots
* see your account
* Create a shot and publish it (Image picker and cropping)
* Edit your published shots
* Save in drafts your current work

## Technologies & pattern
* Kotlin
* MVVM and Observable/Observer pattern
* AndroidX
* Android Architecture Components - ViewModel, LiveData, Room, Paging
* Retrofit - Http client
* Gson - Serialization & deserialization
* Glide - Image loading
* Timber - Logging
* Leak Canary - Memory leak detection
* Butterknife - Data binding
* Rxjava - Data handling
* Dagger2 - Dependency injection

## State
Not stable - architecture refactoring

## Todo
* Manage attachments
* Manage mp4 

## Licence
```
Copyright 2018 Gauvain Seigneur

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```