typedef (ArrayBuffer or ArrayBufferView) BinaryData;

dictionary FontFaceDescriptors {
  CSSOMString style = "normal";
  CSSOMString weight = "normal";
  CSSOMString stretch = "normal";
  CSSOMString unicodeRange = "U+0-10FFFF";
  CSSOMString variant = "normal";
  CSSOMString featureSettings = "normal";
  CSSOMString variationSettings = "normal";
  CSSOMString display = "auto";
};

enum FontFaceLoadStatus { "unloaded", "loading", "loaded", "error" };

[Constructor(CSSOMString family, (CSSOMString or BinaryData) source,
             optional FontFaceDescriptors descriptors),
 Exposed=(Window,Worker)]
interface FontFace {
  attribute CSSOMString family;
  attribute CSSOMString style;
  attribute CSSOMString weight;
  attribute CSSOMString stretch;
  attribute CSSOMString unicodeRange;
  attribute CSSOMString variant;
  attribute CSSOMString featureSettings;
  attribute CSSOMString variationSettings;
  attribute CSSOMString display;

  readonly attribute FontFaceLoadStatus status;

  Promise<FontFace> load();
  readonly attribute Promise<FontFace> loaded;
};

dictionary FontFaceSetLoadEventInit : EventInit {
  sequence<FontFace> fontfaces = [];
};

[Constructor(CSSOMString type, optional FontFaceSetLoadEventInit eventInitDict),
 Exposed=(Window,Worker)]
interface FontFaceSetLoadEvent : Event {
  [SameObject] readonly attribute FrozenArray<FontFace> fontfaces;
};

enum FontFaceSetLoadStatus { "loading", "loaded" };

callback ForEachCallback = void (FontFace font, long index, FontFaceSet self);

[Exposed=(Window,Worker),
 Constructor(sequence<FontFace> initialFaces)]
interface FontFaceSet : EventTarget {
  // FontFaceSet is Set-like!
  setlike<FontFace>;
  FontFaceSet add(FontFace font);
  boolean delete(FontFace font);
  void clear();

  // events for when loading state changes
  attribute EventHandler onloading;
  attribute EventHandler onloadingdone;
  attribute EventHandler onloadingerror;

  // check and start loads if appropriate
  // and fulfill promise when all loads complete
  Promise<sequence<FontFace>> load(CSSOMString font, optional CSSOMString text = " ");

  // return whether all fonts in the fontlist are loaded
  // (does not initiate load if not available)
  boolean check(CSSOMString font, optional CSSOMString text = " ");

  // async notification that font loading and layout operations are done
  readonly attribute Promise<FontFaceSet> ready;

  // loading state, "loading" while one or more fonts loading, "loaded" otherwise
  readonly attribute FontFaceSetLoadStatus status;
};

interface mixin FontFaceSource {
  readonly attribute FontFaceSet fonts;
};

Document includes FontFaceSource;
WorkerGlobalScope includes FontFaceSource;