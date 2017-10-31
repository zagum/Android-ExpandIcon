Android-ExpandIcon
================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android--ExpandIcon-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/4966)
[![JitPack](https://jitpack.io/v/zagum/Android-ExpandIcon.svg)](https://jitpack.io/#zagum/Android-ExpandIcon)
[![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)

Nice and simple customizable implementation of Google style up/down arrow.

![image](https://github.com/zagum/Android-ExpandIcon/blob/master/art/expand_icon_demo.gif)

Another nice example of using this library: [Pixel Slide by hearsilent](https://github.com/hearsilent/PixelSlide)

Compatibility
-------------

This library is compatible from API 15 (Android 4.0.3).

Download
--------

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependency

```groovy
dependencies {
    compile 'com.github.zagum:Android-ExpandIcon:1.2.1'
}
```

Usage
-----

Default implementation:

```xml
    <com.github.zagum.expandicon.ExpandIconView
        android:layout_width="24dp"
        android:layout_height="24dp"/>
```

Fully customized implementation:

```xml
    <com.github.zagum.expandicon.ExpandIconView
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="center_horizontal"
        android:layout_marginTop="56dp"
        app:eiv_animationDuration="300"
        app:eiv_color="#000"
        app:eiv_colorLess="#f00"
        app:eiv_colorMore="#00f"
        app:eiv_colorIntermediate="#0f0"
        app:eiv_roundedCorners="false"
        app:eiv_switchColor="true"
        app:eiv_padding="8dp"/>
```

Public methods: 

```java
    expandIconView.switchState();
    
    expandIconView.setState(ExpandIconView.LESS, true);
    
    expandIconView.setFraction(.3f, true);
    
    expandIconView.setAnimationDuration(2000);
```

See [sample](https://github.com/zagum/Android-ExpandIcon/tree/master/expandicon-sample) project for more information.

License
-------

    Copyright 2016 Evgenii Zagumennyi
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
