(() => {
  "use strict";

  const STORAGE_KEY = "ielts-offline-android-state-v1";

  const readState = () => {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || "{}");
    } catch (_) {
      return {};
    }
  };

  const saveState = () => {
    const currentChapter = document.querySelector("[data-chapter][aria-current='true']");
    const pageMode = document.getElementById("pageViewButton");
    const pageInput = document.getElementById("pageInput");
    const snapshot = {
      chapter: Number(currentChapter ? currentChapter.dataset.chapter : 1) || 1,
      mode: pageMode && pageMode.getAttribute("aria-pressed") === "true" ? "page" : "study",
      page: Math.max(1, Math.min(337, Number(pageInput ? pageInput.value : 1) || 1))
    };
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot));
    } catch (_) {
      // The app remains fully usable if WebView storage is unavailable.
    }
  };

  const pressEscape = () => {
    document.dispatchEvent(new KeyboardEvent("keydown", {key: "Escape"}));
  };

  const syncModeClass = () => {
    const pageMode = document.getElementById("pageViewButton");
    document.body.classList.toggle(
      "android-page-mode",
      !!pageMode && pageMode.getAttribute("aria-pressed") === "true"
    );
  };

  const syncSearchClass = () => {
    const search = document.getElementById("searchInput");
    document.body.classList.toggle(
      "android-searching",
      !!search && !!search.value.trim()
    );
  };

  window.IeltsAndroid = {
    handleBack() {
      const player = document.getElementById("audioPlayer");
      if (player && !player.paused) {
        pressEscape();
        return true;
      }
      if (document.body.classList.contains("menu-open")) {
        pressEscape();
        return true;
      }
      const search = document.getElementById("searchInput");
      if (search && search.value) {
        pressEscape();
        return true;
      }
      const pageMode = document.getElementById("pageViewButton");
      if (pageMode && pageMode.getAttribute("aria-pressed") === "true") {
        const studyButton = document.getElementById("studyViewButton");
        if (studyButton) studyButton.click();
        saveState();
        return true;
      }
      return false;
    },

    pauseForBackground() {
      const player = document.getElementById("audioPlayer");
      if (player && !player.paused) pressEscape();
      saveState();
    }
  };

  const initialize = () => {
    document.documentElement.classList.add("android-app");

    document.addEventListener("click", () => setTimeout(() => {
      saveState();
      syncModeClass();
      syncSearchClass();
    }, 0));
    const savedPageInput = document.getElementById("pageInput");
    if (savedPageInput) savedPageInput.addEventListener("change", saveState);
    const searchInput = document.getElementById("searchInput");
    if (searchInput) searchInput.addEventListener("input", syncSearchClass);
    window.addEventListener("pagehide", saveState);

    const saved = readState();
    const chapter = Number(saved.chapter);
    if (Number.isInteger(chapter) && chapter >= 1 && chapter <= 22) {
      const chapterButton = document.querySelector(`[data-chapter="${chapter}"]`);
      if (chapterButton) chapterButton.click();
    }

    const page = Number(saved.page);
    if (Number.isInteger(page) && page >= 1 && page <= 337) {
      const pageInput = document.getElementById("pageInput");
      if (pageInput) {
        pageInput.value = String(page);
        pageInput.dispatchEvent(new Event("change", {bubbles: true}));
      }
    }

    if (saved.mode === "page") {
      const pageButton = document.getElementById("pageViewButton");
      if (pageButton) pageButton.click();
    }

    syncModeClass();
    syncSearchClass();

    // Keep browser-style selection callouts from covering the image-heavy cards.
    const style = document.createElement("style");
    style.textContent = `
      html.android-app { overscroll-behavior-y: none; }
      html.android-app body { -webkit-tap-highlight-color: rgba(242,103,58,.16); }
      html.android-app img { -webkit-user-drag: none; }
      html.android-app .skip-link { display: none !important; }
      html.android-app body.android-page-mode .hero,
      html.android-app body.android-searching .hero { display: none; }
    `;
    document.head.appendChild(style);
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialize, {once: true});
  } else {
    initialize();
  }
})();
