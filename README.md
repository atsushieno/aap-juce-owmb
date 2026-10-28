# AAP-JUCE port of OWMB (AI slop)

This is a port of [samplaman/owmb](https://github.com/samplaman/owmb) (OpenWav Media Browser) to [Audio Plugins For Android](https://github.com/atsushieno/aap-core), using [aap-juce](https://github.com/atsushieno/aap-juce).

This repository contains only a set of build scripts with almost no creative choices (which is a requirement for any copyright claims) but I leave GPLv3 license text for compatibility with JUCE and aap-juce.

## Building

```
git submodule update --init --recursive
./gradlew :app:assembleDebug
```

`aap-juce-support.patch` is applied to `external/owmb` and JUCE is fetched into `external/owmb/modules/juce` (patched for Android) when Gradle runs the `prepareAapJuce` task.

## Porting notes

- The upstream project fetches JUCE 8.0.6 via `FetchContent`. aap-juce has no patch set for 8.0.6, so the port uses JUCE 8.0.12 instead.
- Only the `Standalone` format is built on Android; AAP loads it through `GetJuceAAPFactoryStandalone`.
- libcurl is not used on Android (JUCE uses the platform HTTP stack there).
- The app declares `INTERNET`: the editor fetches the default Pixeldrain soundbank listing on startup, and without the permission JUCE's Android HTTP stream aborts the process with a pending JNI exception.
- Pixeldrain downloads read the web stream with the `int` overload of `InputStream::read()`. JUCE's Android HTTP stream reports the end with `-1` (and never `isExhausted()`), and the `size_t` overload drops the final partial chunk in that case, so every download ended up shorter than `Content-Length` and was rejected.
- When a host asks for the plugin UI, aap-juce creates a temporary editor just to read its size and deletes it right away. The editor starts a library scan in its constructor, and the scan dialog, tag panel and sample table queued callbacks on a raw `this`, which crashed the plugin process. They now use `SafePointer`, and `LibraryScanner` removes listeners under its listener lock.
- A plugin process that a host loads has no Activity, which JUCE needs for desktop windows (`DialogWindow`, `AlertWindow`) and message boxes. aap-juce's `desktop` JUCE fix attaches them to the editor's view instead. For `FileChooser` (Add Folder and other file dialogs), its `activity` fix starts `JuceFragmentHostActivity`, declared in `AndroidManifest.xml`. On Android 14 and later this works only in hosts that bind plugins with `BIND_ALLOW_ACTIVITY_STARTS`, i.e. hosts built with an aap-core that does so.
- `ThreadWithProgressWindow::runThread()` (audio conversion) needs modal loops, which Android lacks; the port launches the task asynchronously instead.
- The 3D cloud view's GLSL 1.50 shaders are rewritten to GLSL ES 1.00 at runtime, as JUCE creates a GLES 2 context on Android.
