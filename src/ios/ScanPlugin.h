#import <Cordova/CDV.h>
#import "Scan_VC.h"
@interface ScanPlugin:CDVPlugin<ScanDelegate>

@property (nonatomic,copy) NSString * callid_code;

- (void)scan:(CDVInvokedUrlCommand *)command;

@end
