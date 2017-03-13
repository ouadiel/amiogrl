NOM DE L’APPLICATION: Lumio


Auteurs:

	LACHKAR Ouadie
	RIGAL	Nicolas
	GNAZE 	Paul-Emmanuel

N.B: notre équipe fait partie de celles qui sont composées de 3 développeurs.

*************************************************************************************
*
* L’utilisation de l’application
*
*************************************************************************************

.Pour connaître l’état des capteurs:

	1. On active le service en cliquant sur le bouton toggle qui est initialement 	« Désactivé". 
	Le service est alors lancé (interrogations du serveur, prise en charge des
	modifications dans les préférences)

	2. On appuie sur le bouton "GET VALUES" ( X 2 ) pour afficher l’état de tous les 	capteurs 	spécifiés ci-dessus.


.Pour modifier les paramètres de configuration de l’application (préférences):

	1. On appuie sur l’icône en forme de ligne verticale entre-coupée.

	2. On appuie sur "Settings".

	3. L’utilisateur peut modifier les grandeurs suivantes:

		a) Son adresse mail.
		
		b) Les plages horaires:
			
			i) [Début] Pour l’envoi de notifications sur l’application (Semaine)
			ii) [Fin] Pour l’envoi de notifications sur l’application (Semaine)
			iii) [Début] Pour l’envoi de notifications par mails (Semaine)
			iv) [Fin] Pour l’envoi de notification par mails (Semaine)
			v) [Début] Pour l’envoi de notifications par mails (Week-end)
			vi) [Fin] Pour l’envoi de notification par mails (Week-end)


			N.B: Il faudra être attentif au format  d’heure à renseigner.

.L’utilisateur a l’opportunité de tenir le développeur de l’application informé des quelconques bugs/erreurs en appuyant sur le bouton circulaire de couleur rose en bas à droite de l’écran (sur la page principale)

*************************************************************************************
*
* Liste des capteurs
*
*************************************************************************************

Les capteurs utilisés se trouvent dans 5 salles:

	- Salle 2.10
	- Salle 2.05
	- Salle 2.06
	- Salle 2.09
	- Salle 2.08

Ils ont été définis dans l’activité principale du code (le MainActivity).

L’affichage de l’état des capteurs présents dans chaque salle a été mis en oeuvre dans le MainActivity.


L’obtention des valeurs:

1. Le bouton toggle (en bas à gauche de l’écran) doit avoir été activé, afin de lancer le service au préalable. Ainsi les traitements d’interrogation du serveur se feront en arrière plan.

2. L’utilisateur devra appuyer sur le bouton "GET VALUES" deux fois.

Lors de l’interrogation du serveur, on obtient un tableau d’objets JSON parsés (tableau de datum). Dans chaque datum se trouvent des informations suivantes: le mote, sa luminosité, le timestamp.

L’ensemble des datums sont alors stockés dans une variable de type Arraylist nommée "datalist" . Cette variable est alors utilisée pour vérifier l’état des capteurs présents dans leur salle respective.

*************************************************************************************
*
* Notifications
*
*************************************************************************************


L’utilisateur peut être notifié de deux façons différentes:

- Sur l’application:

	- de 19h à 23h en semaine.
	- Le week-end: AUCUNE NOTIFICATION sur l’application


- Par mail

	- de 23h à 6h en semaine.
	- de 19h à 23h le week-end.


La classe privée "CheckChangementBrusque" se servant la méthode privée timeNotif (renvoyant un booléen) est utilisée dans la classe privée ParseJSON pour l’envoie de notification.

CheckChangementBrusque: Permet de vérifier si une notification doit être envoyée, puis elle l’envoie.


*************************************************************************************
*
* Les preférences
*
*************************************************************************************

L’utilisateur a l’opportunité de:

	- modifier à sa guise les plages horaires établis par défaut.
	
	- Renseigner l’adresse mail sur laquelle il souhaiterait recevoir la notification


Pour cette partie du projet, ont été implémentés les éléments suivants:

	- Le layout activity_preference.xml
	- L’activité Settings
	- Les traitements des modifications de l’utilisateurs ont été établis dans l’activité MainService.

*************************************************************************************
*
* Les difficultés rencontrées
*
*************************************************************************************


1. La compréhension du fonctionnement du parseJSON nous a pris un peu de temps, plus particulièrement sur la récupération de données corrects.

2. Nous n’avons pas réussi à modifier une variable (PrefEmail) avec le shared Preferences malgré les tutoriels présents des nos cours et ceux présents sur internet.

3. Nous n’avons pas réussi à arrêter notre timer après le lancement de celui-ci