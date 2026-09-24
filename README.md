# hack 'n' slash

a fastpaced minecraft combat mod built for **forge 1.20.1** focusing on custom weapon mechanics, and crowd control

---

## mechanics

### 1. the scythe
a multi-purpose heavy weapon designed for high-risk, high-reward combat and utility

* **execute attack:**
  * **hold right-click (1 sec)** to charge the scythe (plays windup audio).
  * release to **charge your next melee hit**.
  * landing a charged attack on an enemy at or below **15% max HP** triggers an **execute**:
    * instantly slays the target (`target.getHealth() + 1.0F` true damage)
    * restores **1 heart** ($\text{2.0 HP}$) to the attacker
  * *note: Releasing the charge applies a 1.5-second cooldown to prevent spam*

* **utility & farming:**
  * **3x3 auto-replant and harvester:** right-clicking mature crops harvests and automatically replants a $3 \times 3$ area.
  * **multitool:** functions as a pickaxe/axe hybrid for block drops and mining speeds. can also strip logs like an axe.

---

### 2. soul pull (enchantment)
a scythe-exclusive enchantment designed to pull fleeing targets back into melee range.

* **target:** exclusive to `ScytheItem` via enchanting table / anvils.
* **max Level:** III
* **effect:** on hit, it drags the target horizontally toward the attacker with scaling force:
  * **level I:** $0.35$ pull strength
  * **level II:** $0.50$ pull strength
  * **level III:** $0.65$ pull strength
* also spawns soul particles

---

## requirements & setup
* **minecraft:** `1.20.1`
* **mod loader:** `Forge`

---

## credits
* rocket launcher model and texture: `Iron Minecart42` on [sketchfab](https://sketchfab.com/3d-models/ultrakill-rocket-launcher-remake-fixed-fan-art-0c7ac9650b55416da816656ca8e6a5ec)