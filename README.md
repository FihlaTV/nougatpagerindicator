Nougat Pager Indicator
==========================

Nougat Pager Indicator is a simple library which ports pager indicator from notification tray which 
first appeared on Android N.

![Pager Indicator Sample][1]

It can be directly used with `ViewPager` or you can control it on your own. For sample usage, head
to `sample/` folder.


Getting started
---------------

  Include dependency into your project.
  
    compile 'io.github.mroczis:nougatpagerindicator:1.0.0'
    
    
  It is available on `jcenter`, so do not forget to link it in your root `build.gradle`.
  
    allprojects {
        repositories {
            jcenter()
        }
    }

  Add `PagerIndicator` to XML anywhere you like.

    <cz.mroczis.nougatpagerindicator.PagerIndicator
        android:id="@+id/indicator"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:npiActiveDot="1"
        app:npiActiveDotColor="@color/colorOrange"
        app:npiInactiveDotColor="@color/colorGray"
        app:npiDotRadius="3dp"
        app:npiDotSpacing="6dp"
        app:npiDotsCount="5"
        />

  Attach `PagerIndicator` to instance of your `ViewPager`

    final PagerIndicator indicator = findViewById(R.id.indicator);
    final ViewPager pager = findViewById(R.id.pager);
    
    indicator.setupWithViewPager(pager);
       


License
-------

    Copyright 2017 Michal Mroček
    
    Licensed under the Apache License, Version 2.0 (the "License");

    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    

    http://www.apache.org/licenses/LICENSE-2.0

    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and






 [1]: https://raw.githubusercontent.com/mroczis/nougatpagerindicator/master