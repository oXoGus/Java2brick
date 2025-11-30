# java2brick

### Fonctionnement du programme

Ce programme Java prend en argument un dossier, un numéro de pavage et les dimensions du pavage souhaité. Le programme ne fait pas le pavage directement, mais crée tous les fichiers nécessaires pour que le programme C réalise le pavage.

Tout d'abord, le programme réduit la résolution de l'image aux dimensions souhaitées avec plusieurs méthodes d'interpolation au choix (plus proche voisin, interpolation linéaire et interpolation bicubique). Par défaut, la méthode d'interpolation utilisée est la bicubique.

Le programme Java crée donc un fichier de stock de briques, en récupérant les données depuis la base de données MySQL, et le formate en fichier texte comme demandé par le programme C.
Il crée ensuite le fichier texte contenant l'image compressée au format demandé par le programme C.

Le programme Java exécute le programme C avec un `ProcessBuilder` et récupère sa sortie standard (stdout) pour afficher l'avancement de la création du pavage.

Une fois le pavage créé par le programme C, le programme Java lit le fichier où le C a écrit le pavage et insère toutes les pièces du pavage dans la base de données.
Une fois le pavage sauvegardé dans la base de données, le programme crée une image de prévisualisation du pavage généré.
Cette image sera ensuite affichée par le PHP à l'utilisateur sur le site.

### Instructions pour exécuter le programme

- Tout d'abord, il faut créer le JAR avec la commande `mvn clean install`.
- Comme le Java appelle le programme C, il faut aussi compiler ce dernier avec la commande : `gcc main.c -o main -lm`.
- Vous pouvez maintenant exécuter le JAR avec la commande :
  `java -jar target/Java2brick-1.0-SNAPSHOT.jar [chemin vers le dossier contenant les pavages] [code du pavage (nom du dossier dans le dossier contenant les pavages)] [largeur du pavage souhaité] [hauteur du pavage souhaité]`
- Pour les données de test, vous pouvez vous servir du dossier `testPavings` avec le code de pavage `692c6885ae7811.23917055`.
- Si vous voulez tester avec une nouvelle image, vous pouvez retirer la vérification dans la base de données du pavage et ajouter un nouveau dossier dans `testPavings` contenant l'image que vous voulez transformer, renommée en `original-image`.
- Vous pouvez ensuite voir le résultat dans votre dossier ajouté en regardant l'image `paving-preview`.
