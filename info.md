# Projekt – Git & GitHub Guide (kort)

En enkel och tydlig guide för hur alla i teamet ska arbeta med Git och GitHub.

---

## Folder structure (.gitkeep)

Git does not track empty folders. We keep the folder/package structure by adding empty placeholder files named
`.gitkeep`.
Do not delete them unless the folder contains real code/files.

## 🔧 Första gången

1. Klona (ladda ner) projektet:

```bash
git clone https://github.com/David-refai/E-commerce_project.git
cd <REPO_NAME>
```

Kontrollera att Git är installerat:

```
git --version
```

Lägg in ditt namn och din e-post (en gång per dator):

```bash
git config --global user.name "Ditt Namn"
git config --global user.email "din.epost@example.com"
```

# 🌿 Brancher (arbetsgrenar)

## Jobba aldrig direkt på main.

- Skapa en egen branch för ditt arbete.
- Skapa en ny branch:

```bash
git checkout main
git pull origin main
git checkout -b feature/kort-beskrivning
```

# ✍️ Spara ändringar (commit)

Gör commits ofta och skriv korta, tydliga meddelanden:

```bash
git add .
git commit -m "Kort beskrivning av ändringen"
```

<a id="issues-link"></a>

# 🐞 Koppla jobben till Issues

Om du vill att en Issue ska stängas automatiskt när PR:en mergas, skriv i commit-meddelandet eller i Pull
Request-beskrivningen:

```bash
Closes #ID      1/3/4/...

Du kan också skriva:
Fixes #ID       3/5/6/...
Resolves #ID    2/3/4/5/..
```

## Alla fungerar, och du kan alltså välja: Closes, Fixes eller Resolves + Issue-numret.

# ⚠️ Om issue redan är stängd?

Då händer inget mer (den förblir stängd).

Men PR/commit kommer fortfarande att länkas i historiken.

Dessa ord öppnar inte en redan stängd Issue och ändrar inget – bara historisk koppling.

För auto-close → se till att Issue är öppen när PR:en mergas.

## ⬆️ Skicka din branch till GitHub (push)

git push origin feature/kort-beskrivning

## 🔀 Pull Request (PR)

När ditt jobb är klart:

- Pusha din branch.
- Gå till GitHub och öppna en PR.
- I PR-beskrivningen kan du skriva så här:

## Sammanfattning

- Kort beskrivning av ändringen.

## Test

- [x] Testat lokalt.
  Closes #12  (eller Fixes #12 / Resolves #12)

# 🔁 Uppdatera din branch om main ändras

Om någon annan merger ny kod innan dig:

```bash
git checkout main
git pull origin main
git checkout feature/kort-beskrivning
git rebase main
```

# Vid konflikt:

Öppna filen och fixa problemet, fortsätt sedan:

```bash
git add <fil>
git rebase --continue
```

Eller avbryt om det blir svårt:

```bash
git rebase --abort
```

# ❌ Gör inte

```
❌ Commit/push direkt till main
❌ Pusha trasig kod som inte kompilerar eller där tester failar
```

# ✅ Bra vana

Korta, tydliga commits

Branch-namn med prefix:

```
feature/...

bugfix/...

hotfix/...

refactor/...
```

Koppla arbete till Issues genome [👆](#issues-link)

Använd PR + review innan merge

