# Komunikační klient a server v Javě

## Funkcionalita
Projekt obsahuje jak serverovou část, tak klienta.
Komunikace probíhá na úrovni TCP socketů. Každý klient se připojuje na server, pomocí nastavení IP adresy a portu.


### Server
Aplikace bez grafického rozhraní, pouze konzolový vstup a výstup.
Po spuštění lze nastavit číslo portu pro naslouchání nových klientů.

Každý připojený klient vytvoří samostatné vlákno, které má za úkol příjem veškeré komunikace od daného klienta.
Pokud klient vytvoří novou místnost, tato místnost bude reprezentována vláknem, rozesílá veškerou komunikaci všem klientům, kteří se do místnosti připojí.
Ve chvíli kdy je místnost prázdná, se vlákno ukončí. Místnost je však nadále uchována v databázi až do jejího smazání, které může provést pouze tvůrce místnosti.
Po připojení do místnosti, které je prázdná, se opět spustí vlákno této místnosti.

Uživatel, který byl off-line a neopustil místnost, obdrží veškeré zprávy z této doby, jakmile bude opět online.

### Klient
Grafické rozhraní umožnuje nastavit IP adresu a port serveru, ke kterému se klient připojí.
Tuto volbu lze uložit.  
Po připojení na server se uživatel přihlásí svou přezdívkou a heslem.  

Přihlášenému uživateli se zobrazí seznam všech místností, ve kterých je připojen. Dále má možnost připojit se do již existujících místností, nebo vytvořit novou.
Místnost lze chránit heslem a vytvořit ji tak soukromou pro specifické uživatele.

U každé místnosti je zobrazen seznam připojených / online uživatelů.
Majitel místnosti má možnost vyloučit ostatní uživatele z místnosti.

