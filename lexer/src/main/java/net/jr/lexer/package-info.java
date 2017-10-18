/**
 *
 On génère pour chaque symbole un automate qui reconnait l'expression rationnelle associée au symbole. Cet automate sera identifié par le symbole.
 Tant que le mot n'a pas été entièrement analysé et qu'il n'y a pas d'erreur :
 On lit le mot lettre par lettre en faisant avancer les automates en parallèle pour chaque lettre.
 Lorsqu'un automate entre dans un état final, on conserve le sous-mot trouvé et l'identificateur de l'automate.
 Si tous les automates sont dans un état puits ou que le mot a été entièrement analysé :
 Si aucun automate n'a atteint d'état final : on renvoie une erreur.
 Sinon, on ajoute le couple (plus grand sous-mot avec un automate en état final, type de l'automate l'ayant trouvé) à la liste des entités lexicales. On se replace alors juste après ce sous-mot, on remet les automates à zéro et on continue la lecture du mot.

 */
package net.jr.lexer;