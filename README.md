# VCalendar - Advanced Android RecyclerView infinite calendar view

## Features
 * Customzing everything and everywhere
 * Day decorators
 * Few selection modes: single, multiple, range, even(todo), odd(todo)
 * Handling attaching/removing lifecycler (visible month view will be attached, out of screen - detached)
 
 
## Installing
```groovy
// in root module
allprojects {
    repositories {
        maven { url "https://dl.bintray.com/edwardstock/vcalendar" }
    }
}
```

```groovy
// in project module
dependencies {
    implementation 'com.edwardstock:vcalendar:1.0.0'
}
```


## Using
 ### Initializing
```java
// in your Application class
import com.edwardstock.vcalendar.VCalendar;
class App extends Application {
    
   public void onCreate() {
       super.onCreate();
       VCalendar.initialize(this);
   } 
}
```

### Layout
```xml
<com.edwardstock.vcalendar.VCalendar
    android:id="@+id/cal"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:enableLegend="true"
    app:orientation="vertical"
    app:selectionBeginBackground="@drawable/bg_calendar_day_selection_begin"
    app:selectionEndBackground="@drawable/bg_calendar_day_selection_end"
    app:selectionMiddleBackground="@drawable/bg_calendar_day_selection_middle"
    app:selectionMode="range"
    app:selectionSingleBackground="@drawable/bg_calendar_day_selection_single"
/>
```