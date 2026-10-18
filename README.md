# मारवाड़ी कीबोर्ड (Android)

Roman/English अक्षरों से मारवाड़ी टाइप करने के लिए शुरुआती Android system keyboard.

## अभी क्या काम करता है
- असली Android system keyboard (IME), केवल preview नहीं।
- QWERTY, Shift, Backspace, Space, Enter और symbols.
- स्थानीय शब्दकोश, क्षेत्रीय वर्तनी के variants और सीमित one-character typo suggestions.
- हिंदी setup screen और offline word-by-word English meanings.
- Password fields में suggestions बंद; typed text store या log नहीं किया जाता।

## अभी बाकी है
- प्राकृतिक sentence translation के लिए सुरक्षित HTTPS backend/API.
- मारवाड़ी बोलने वालों से dictionary spelling review.
- अलग app icon, emoji/voice input और वास्तविक फोन/WhatsApp testing.

## Build और install
GitHub Actions में Prepare Marwadi source and build APK workflow debug APK बनाकर artifact अपलोड करता है। APK install करें, ऐप खोलें, कीबोर्ड चालू करें और Android settings में enable करके कीबोर्ड चुनें।

## Dictionary और privacy
marwari_dictionary.json शुरुआती सूची है, आधिकारिक मानक नहीं; क्षेत्र के अनुसार वर्तनी बदल सकती है। Keyboard text store, log या server पर नहीं भेजता। Offline word-meaning helper डिवाइस पर चलता है। Natural sentence translation अभी configured नहीं है।
