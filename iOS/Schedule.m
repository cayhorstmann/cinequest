//
//  Schedule.m
//  CineQuest
//
//  Created by Loc Phan on 10/23/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "Schedule.h"


@implementation Schedule

@synthesize title;
@synthesize type;
@synthesize venue;
@synthesize date;
@synthesize endDate;
@synthesize ID;
@synthesize prog_id;
@synthesize dateString;
@synthesize timeString;
@synthesize isSelected;
@synthesize fontColor;
@synthesize endTimeString;

- (void)dealloc {
	[dateString release];
	[timeString release];
	[endTimeString release];
	[title release];
	[type release];
	[venue release];
	[date release];
	[endDate release];
	[fontColor release];
	
    [super dealloc];
} 

@end
