# Vícevrstvý Perceptron pro Automatizaci Domácnosti

Tento projekt demonstruje praktickou aplikaci umělé inteligence v rámci simulace inteligentní domácnosti. **Vícevrstvý perceptron** řídí aktivitu ventilátoru (větrání/klimatizace) a servomotoru (žaluzie/stínění) na základě aktuálních podmínek: **teploty** a **intenzity světla**.

---

## Cíl projektu

Cílem je vytvořit jednoduchý, datově orientovaný systém automatizace, který nahrazuje tradiční programování pevnými pravidly. Model se učí z historických dat, aby byl schopen předpovídat vhodné reakce (zapnutí ventilátoru a natočení žaluzií) na nové senzorové vstupy.

## Hardwarové komponenty

Systém je postaven na mikropočítači **BBC Micro:bit V2** se sadou periferií:

* **Micro:bit V2:** Využívá integrovaný snímač **teploty** a snímač **osvětlení**.
* **Servomotor (180°):** Simuluje pohyb žaluzií (3 polohy: 0°, 45°, 90°).
* **5V motor s větráčkem:** Simuluje funkčnost ventilátoru/klimatizace (3 úrovně rychlosti).
* **Rozšiřující modul a Kontaktní pole:** Pro stabilní a modulární propojení pinů.


### Výhody hardwarového řešení
* **Modularita:** Snadné připojení a změna komponent díky rozšiřujícím modulům.
* **Přenositelnost:** Micro:bit je malý a energeticky efektivní pro prototypování.

---

## Softwarové Řešení a Tok dat

Projekt je rozdělen na dvě hlavní části: Micro:bit (sběr dat) a IntelliJ (trénink a predikce).

### 1. Sběr a Příprava Dat

* **Micro:bit:** Měří teplotu a intenzitu světla každých **10 sekund** a odesílá je jako **JSON řetězec** přes sériový port (např. `{"Teplota": 27, "Svetlo": 185}`).
* **IntelliJ (Java):** Čte a parsuje data ze sériového portu.
* **Normalizace a Úprava:** Shromážděná data jsou doplněna o očekávané výstupy pro trénování (3 úrovně rychlosti ventilátoru a 3 úrovně natočení žaluzií). Datová sada je **standardizována** pro rovnoměrné zastoupení všech očekávaných reakcí (cca 1000 řádků).

### 2. Struktura Neuronové Sítě (Deeplearning4j)

Síť je implementována jako **vícevrstvý perceptron** (MLP) s regresními výstupy:

| Vrstva | Počet Neuronů | Aktivační Funkce | Použité Techniky |
| :--- | :--- | :--- | :--- |
| **Vstupní** | 2 (Teplota, Světlo) | N/A | Normalizace dat |
| **Skrytá 1** | 64 | ReLU | Batch Normalization |
| **Skrytá 2** | 128 | ReLU | Dropout (50%), Batch Normalization |
| **Skrytá 3** | 256 | ReLU | Dropout (50%), Batch Normalization |
| **Výstupní** | 6 (3x Rychlost, 3x Pozice) | Identity | Loss Function: **MSE** |

#### Optimalizace:
* **Optimalizátor:** Adaptivní algoritmus **Adam**.
* **Regularizace:** L2 regularizace ($0.0001$) a **Dropout** (prevence přeučení).
* **Early Stopping:** Trénink se zastaví, pokud se ztrátová funkce nezlepší po definovaném počtu epoch (10).

### 3. Predikce a Realizace v Reálném Čase

Po natrénování je model uložen (`trained_model.zip`). Celý cyklus se opakuje každých **30 sekund**.

1.  **Sběr dat:** Micro:bit odesílá aktuální hodnoty (Teplota, Světlo).
2.  **Predikce:** Model v IntelliJ (Deeplearning4j) přijme normalizované vstupy. Výstup (6 neuronů) je zpracován funkcí **softmax** pro určení predikovaných tříd (rychlost ventilátoru $0/1/2$ a pozice žaluzií $0/1/2$).
3.  **Odeslání zpět:** Výsledek je odeslán zpět do Micro:bitu jako řetězec (např. `"1,2"`).
4.  **Realizace:** Micro:bit parsuje zprávu. Používá `pins.analogWritePin` pro řízení rychlosti ventilátoru a `pins.servoWritePin` pro nastavení úhlu žaluzií. Zobrazuje aktuální stav na displeji.
